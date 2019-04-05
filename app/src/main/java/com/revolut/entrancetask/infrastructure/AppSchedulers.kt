package com.revolut.entrancetask.infrastructure

import io.reactivex.Scheduler

class AppSchedulers(
    val io: Scheduler,
    val ui: Scheduler
)