package com.vano.kiki.engine

import com.topjohnwu.superuser.Shell

object TouchInjector {
    fun tap(x: Int, y: Int) {
        val safeX = x.coerceAtLeast(0)
        val safeY = y.coerceAtLeast(0)
        Shell.cmd("input tap $safeX $safeY").submit()
    }
}
