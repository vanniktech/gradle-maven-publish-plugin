#!/usr/bin/env kotlin

@file:DependsOn("com.freeletics.gradle:scripts-formatting:0.24.0")

import com.freeletics.gradle.scripts.KtLintCli
import com.github.ajalt.clikt.core.main

KtLintCli().main(args)
