/*
 * Copyright 2023 International Business Machines Corp..
 * 
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.ibm.slsa.test;

import java.io.File;

public class Constants {

    public static final String RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources" + File.separator;

    public static final String FILE_NAME_SIMPLE_TXT = "simple.txt";
    public static final String FINAL_NAME_APP = "app";
    public static final String PACKAGE_TYPE = "ear";
    public static final String FILE_NAME_APP_WAR = FINAL_NAME_APP + "." + PACKAGE_TYPE;

    public static final String FILE_PATH_SIMPLE_TXT = RESOURCES_DIR + FILE_NAME_SIMPLE_TXT;
    public static final String FILE_PATH_APP_WAR = RESOURCES_DIR + FILE_NAME_APP_WAR;

    public static final String SHA_FILE_SIMPLT_TXT = "38d141b35057bbb691b9756c20a6c31a0ab0bbf2076538a7fb6d9ee8835096d7";
    public static final String SHA_APP_WAR = "76f7613c5fbb7320d4aaee3ed17a58d71a5e03783ae5db222687ac0e3fffa0a2";

}
