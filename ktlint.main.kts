#!/usr/bin/env kotlin

@file:DependsOn("com.freeletics.gradle:scripts-formatting-jvm:0.36.3")

import com.freeletics.gradle.scripts.KtLintCli
import com.github.ajalt.clikt.core.main

KtLintCli().main(args)
