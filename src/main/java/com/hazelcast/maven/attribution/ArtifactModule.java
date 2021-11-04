package com.hazelcast.maven.attribution;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.nio.file.Path;

import org.codehaus.plexus.languages.java.jpms.JavaModuleDescriptor;
import org.codehaus.plexus.languages.java.jpms.ModuleNameSource;

public class ArtifactModule {
    private final String ga;

    private final File artifactFile;

    private final Path sourceJarPath;

    private final JavaModuleDescriptor moduleDescriptor;

    private final ModuleNameSource moduleNameSource;

    public ArtifactModule(String ga, File artifactFile, Path sourceJarPath) {
        this(ga, artifactFile, sourceJarPath, null, null);
    }

    public ArtifactModule(String ga, File artifactFile, Path sourceJarPath, JavaModuleDescriptor moduleDescriptor,
            ModuleNameSource moduleNameSource) {
        this.ga = ga;
        this.artifactFile = artifactFile;
        this.sourceJarPath = sourceJarPath;
        this.moduleDescriptor = moduleDescriptor;
        this.moduleNameSource = moduleNameSource;
    }

    public String getGa() {
        return ga;
    }

    public Path getSourceJarPath() {
        return sourceJarPath;
    }

    public File getArtifactFile() {
        return artifactFile;
    }

    public JavaModuleDescriptor getModuleDescriptor() {
        return moduleDescriptor;
    }

    public ModuleNameSource getModuleNameSource() {
        return moduleNameSource;
    }
}
