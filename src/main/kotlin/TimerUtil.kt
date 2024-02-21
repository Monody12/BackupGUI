package org.example

import java.util.Timer

object TimerUtil {
    /**
     * 开始一个定时任务
     * 延迟delay毫秒后，执行第一次task，然后每隔period毫秒执行一次task
     */
    fun startTimer(timer: Timer, delay: Long, period: Long, action: () -> Unit) {
        timer.scheduleAtFixedRate(object : java.util.TimerTask() {
            override fun run() {
                action()
            }
        }, delay, period)
    }

    /**
     * 停止一个定时任务
     */
    fun stopTimer(timer: Timer) {
        timer.cancel()
    }
}