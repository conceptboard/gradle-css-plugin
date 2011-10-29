/**
 * Copyright 2011 Eric Wendelin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.eriwen.gradle.css.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.eriwen.gradle.css.CssMinifier

class CssTask extends DefaultTask {
    private static final String COMBINED_CSS_FILE = 'combined.css'
    private static final String TMP_DIR = 'tmp/css'
    private static final CssMinifier MINIFIER = new CssMinifier()

    Integer lineBreakPos = -1

    @TaskAction
    def run() {
        final File tempDir = makeTempDir()
        final def outputFiles = getOutputs().files.files.toArray()
        final String outputPath = (outputFiles[0] as File).canonicalPath
        final String tempPath = "${tempDir.canonicalPath}/${COMBINED_CSS_FILE}"

        if (outputFiles.size() != 1) {
            throw new IllegalArgumentException('Output must be exactly 1 File object. Example: outputs.file = file("myFile")')
        }

        ant.concat(destfile: tempPath) {
            getInputs().files.files.each {
                fileset(dir: it, includes: '*.css')
            }
        }

        MINIFIER.minifyCssFile(new File(tempPath), outputFiles[0] as File, lineBreakPos)

        ant.gzip(src: outputPath, destfile: "${outputPath}.gz")
        ant.move(file: "${outputPath}.gz", tofile: outputPath)
    }

    File makeTempDir() {
        File tempDir = new File(project.buildDir, TMP_DIR)
        tempDir.mkdirs()
        return tempDir
    }
}