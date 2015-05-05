/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Sample Testacular configuration file, that contain pretty much all the available options
// It's used for running client tests on Travis (http://travis-ci.org/#!/karma-runner/karma)
// Most of the options can be overriden by cli arguments (see karma --help)
//
// For all available config options and default values, see:
// https://github.com/karma-runner/karma/blob/stable/lib/config.js#L54

module.exports = function (config) {
    config.set({
            basePath: '../../out/test/kotlin.web.demo.frontend',
            frameworks: ['qunit'],
            files: [
                'lib/kotlin.js',
                'kotlin.web.demo.frontend.js'
            ],
            exclude: [],
            port: 9876,
            runnerPort: 9100,
            colors: true,
            autoWatch: false,
            browsers: [
                'PhantomJS'
            ],
            captureTimeout: 5000,
            singleRun: true,
            reportSlowerThan: 500,
            preprocessors: {
                '**/*.coffee': 'coffee'
            }
        }
    )
};