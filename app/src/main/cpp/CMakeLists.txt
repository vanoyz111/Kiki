cmake_minimum_required(VERSION 3.22.1)
project(kikidaemon)

add_executable(kikidaemon kikidaemon.cpp)

# Nama file output disamarin jadi .so biar ke-bundle otomatis ke APK
# oleh packaging native Android (isinya tetep executable biasa)
set_target_properties(kikidaemon PROPERTIES
    PREFIX "lib"
    SUFFIX ".so"
)

target_link_libraries(kikidaemon log)
