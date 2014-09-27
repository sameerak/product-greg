/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//Configs needed for controllers
var configs = require('../config.json');

var store = require('store');
store.server.init(configs);

store.user.init(configs);

//configs needed for impact analysis
var carbon=require('carbon');
var server=new carbon.server.Server({
    url : "https://localhost:9443/"
});

var sysRegistry=new carbon.registry.Registry(server, {
    system: true,
    tenantId: -1234
});

var governanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
var configurations = governanceUtils.findGovernanceArtifactConfigurations(sysRegistry.registry);
var config = new Object();
config["application/vnd.wso2.endpoint"] = "endpoint.png";

for (var i = 0; i < configurations.size(); i++) {
    config[configurations.get(i).getMediaType()] = "list" + configurations.get(i).getIconSet() + ".png";
}
application.put("mediaTypesToImages", config);