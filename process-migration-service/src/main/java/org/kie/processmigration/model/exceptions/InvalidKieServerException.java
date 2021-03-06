/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.model.exceptions;

public class InvalidKieServerException extends InvalidMigrationException {

    private static final long serialVersionUID = -7640655290779519838L;

    public InvalidKieServerException(String kieServerId) {
        super(kieServerId);
    }

    @Override
    public String getMessage() {
        return String.format("Invalid KIE Server provided: %s", super.getMessage());
    }

}
