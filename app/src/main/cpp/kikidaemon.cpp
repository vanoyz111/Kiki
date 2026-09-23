#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cerrno>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <linux/uinput.h>
#include <linux/input.h>
#include <android/log.h>

#define LOG_TAG "kikidaemon"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static const char* PIPE_PATH = "/data/local/tmp/keymapper_pipe";
static int uinputFd = -1;

static void emit(int fd, int type, int code, int value) {
    struct input_event ev{};
    ev.type = type;
    ev.code = code;
    ev.value = value;
    write(fd, &ev, sizeof(ev));
}

static bool setupUinputDevice(int screenW, int screenH) {
    uinputFd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
    if (uinputFd < 0) {
        LOGE("Gagal buka /dev/uinput: %s", strerror(errno));
        return false;
    }

    ioctl(uinputFd, UI_SET_EVBIT, EV_KEY);
    ioctl(uinputFd, UI_SET_KEYBIT, BTN_TOUCH);

    ioctl(uinputFd, UI_SET_EVBIT, EV_ABS);
    ioctl(uinputFd, UI_SET_ABSBIT, ABS_MT_SLOT);
    ioctl(uinputFd, UI_SET_ABSBIT, ABS_MT_TRACKING_ID);
    ioctl(uinputFd, UI_SET_ABSBIT, ABS_MT_POSITION_X);
    ioctl(uinputFd, UI_SET_ABSBIT, ABS_MT_POSITION_Y);

    struct uinput_user_dev uidev{};
    memset(&uidev, 0, sizeof(uidev));
    snprintf(uidev.name, UINPUT_MAX_NAME_SIZE, "kiki-virtual-touch");
    uidev.id.bustype = BUS_VIRTUAL;
    uidev.id.vendor = 0x1234;
    uidev.id.product = 0x5678;
    uidev.id.version = 1;

    uidev.absmin[ABS_MT_SLOT] = 0;
    uidev.absmax[ABS_MT_SLOT] = 9;
    uidev.absmin[ABS_MT_TRACKING_ID] = 0;
    uidev.absmax[ABS_MT_TRACKING_ID] = 65535;
    uidev.absmin[ABS_MT_POSITION_X] = 0;
    uidev.absmax[ABS_MT_POSITION_X] = screenW;
    uidev.absmin[ABS_MT_POSITION_Y] = 0;
    uidev.absmax[ABS_MT_POSITION_Y] = screenH;

    if (write(uinputFd, &uidev, sizeof(uidev)) < 0) {
        LOGE("Gagal tulis uinput_user_dev: %s", strerror(errno));
        return false;
    }

    if (ioctl(uinputFd, UI_DEV_CREATE) < 0) {
        LOGE("UI_DEV_CREATE gagal: %s", strerror(errno));
        return false;
    }

    LOGI("Virtual touch device siap (%dx%d)", screenW, screenH);
    return true;
}

static void sendTap(int x, int y) {
    static int trackingId = 1;

    emit(uinputFd, EV_ABS, ABS_MT_SLOT, 0);
    emit(uinputFd, EV_ABS, ABS_MT_TRACKING_ID, trackingId++);
    emit(uinputFd, EV_ABS, ABS_MT_POSITION_X, x);
    emit(uinputFd, EV_ABS, ABS_MT_POSITION_Y, y);
    emit(uinputFd, EV_KEY, BTN_TOUCH, 1);
    emit(uinputFd, EV_SYN, SYN_REPORT, 0);

    usleep(16000);

    emit(uinputFd, EV_ABS, ABS_MT_SLOT, 0);
    emit(uinputFd, EV_ABS, ABS_MT_TRACKING_ID, -1);
    emit(uinputFd, EV_KEY, BTN_TOUCH, 0);
    emit(uinputFd, EV_SYN, SYN_REPORT, 0);
}

static void handleCommand(const char* line) {
    int x, y;
    if (sscanf(line, "TAP %d %d", &x, &y) == 2) {
        sendTap(x, y);
        return;
    }
    LOGE("Command gak dikenali: %s", line);
}

int main(int argc, char** argv) {
    int screenW = 1080;
    int screenH = 2400;
    if (argc >= 3) {
        screenW = atoi(argv[1]);
        screenH = atoi(argv[2]);
    }

    LOGI("kikidaemon mulai, layar %dx%d", screenW, screenH);

    if (!setupUinputDevice(screenW, screenH)) {
        LOGE("Setup uinput gagal, daemon berhenti");
        return 1;
    }

    unlink(PIPE_PATH);
    if (mkfifo(PIPE_PATH, 0666) != 0) {
        LOGE("mkfifo gagal: %s", strerror(errno));
        return 1;
    }
    chmod(PIPE_PATH, 0666);

    LOGI("Dengerin pipe di %s", PIPE_PATH);

    char buffer[256];
    while (true) {
        int pipeFd = open(PIPE_PATH, O_RDONLY);
        if (pipeFd < 0) {
            LOGE("Gagal buka pipe: %s", strerror(errno));
            usleep(500000);
            continue;
        }

        FILE* pipeFile = fdopen(pipeFd, "r");
        if (!pipeFile) {
            close(pipeFd);
            continue;
        }

        while (fgets(buffer, sizeof(buffer), pipeFile)) {
            size_t len = strlen(buffer);
            if (len > 0 && buffer[len - 1] == '\n') buffer[len - 1] = '\0';
            handleCommand(buffer);
        }

        fclose(pipeFile);
    }
}
