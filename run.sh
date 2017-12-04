#!/bin/sh

set -x
java -cp out ca.thoughtflow.concurrency.Benchmark 1000 1000000 1 ca.thoughtflow.concurrency.MultiThreadedPrimeCounter ca.thoughtflow.concurrency.CountDownLatchPrimeCounter ca.thoughtflow.concurrency.CachedThreadPoolPrimeCounter ca.thoughtflow.concurrency.ForkJoinPrimeCounter ca.thoughtflow.concurrency.PromisePrimeCounter ca.thoughtflow.concurrency.SpliteratorPrimeCounter ca.thoughtflow.concurrency.ParallelStreamPrimeCounter ca.thoughtflow.concurrency.ReactiveStreamPrimeFinder
