/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
asset.manager = function(ctx) {   
    var setCustomAssetAttributes = function(asset, userRegistry) {
        var wadlUrl=asset.attributes.interface_wadl;
        if (wadlUrl != null) {
            try {
                var resource = userRegistry.registry.get(wadlUrl);
                var wadlContent = getInterfaceTypeContent(resource);
                asset.wadlContent = wadlContent;
            } catch(e) {
                asset.wadlContent = "";
            }
        }
    }; 

	var setDependencies = function(genericArtifact, asset ,userRegistry) {
        var dependencyArtifacts = genericArtifact.getDependencies();
        asset.dependencies = getAssociations(dependencyArtifacts, userRegistry);
    };

    var setDependents = function(genericArtifact, asset, userRegistry) {
        var dependentArtifacts = genericArtifact.getDependents();
        asset.dependents = getAssociations(dependentArtifacts, userRegistry);
    };

    var getRegistry = function(cSession) {
        var userMod = require('store').user;
        var server = require('store').server;
        var user = server.current(cSession);
        var userRegistry;
        if (user) {
            userRegistry = userMod.userRegistry(cSession);
        } else {
            userRegistry = server.anonRegistry(tenantId);
        }
        return userRegistry;
    };

    var getInterfaceTypeContent = function (resource) {
        var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
        var content = resource.getContent();
        var value = '' + new Stream(new ByteArrayInputStream(content));
        return value;
    };

    var getAssociations = function(genericArtifacts, userRegistry){
        //Array to store the association names.
        var associations = [];
        if (genericArtifacts != null) {
            for (var index in genericArtifacts) {
                var deps = {};
                var path = genericArtifacts[index].getPath();
                var resource = userRegistry.registry.get('/_system/governance' + path);
                var mediaType = resource.getMediaType();
                var name = genericArtifacts[index].getQName().getLocalPart();
                var govUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils
                var keyName = govUtils.getArtifactConfigurationByMediaType(getRegistry(ctx.session).registry, mediaType).getKey();
                var subPaths = path.split('/');
                var associationName = name;
                var associationUUID = resource.getUUID();
                deps.associationName = associationName;
                deps.associationType = keyName;
                deps.associationUUID = associationUUID;

                if(deps.associationType == "wadl") {
                    associations.push(deps);
                }
                if(deps.associationType == "swagger") {
                    associations.push(deps);
                }
            }
        }
        return associations;
    };

    return {
        search: function(query, paging) {
            var assets = this._super.search.call(this, query, paging);
            return assets;
        },
        get: function(id) {
            var asset = this._super.get.call(this, id);
            var userRegistry = getRegistry(ctx.session);
            try {
                setCustomAssetAttributes(asset, userRegistry);
            } catch (e){}

            //get the GenericArtifactManager
            var rawArtifact = this.am.manager.getGenericArtifact(id);
            try {
                setDependencies(rawArtifact, asset, userRegistry);
            } catch (e){}
            try {
                setDependents(rawArtifact, asset, userRegistry);
            } catch (e){

            }

            return asset;
        }
    };
};

asset.configure = function () {
    return {
        meta: {
            ui: {
                icon: 'fw fw-rest-service',
                iconColor: 'purple'
            }
        }
    }
};

asset.renderer = function(ctx){
    return {
        pageDecorators:{
            downloadPopulator:function(page){
                //Populate the links for downloading content RXTs
                if(page.meta.pageName === 'details'){
                    var config = require('/config/store.js').config();
                    var pluralType = 'wadls'; //Assume it is a WADl
                    page.assets.downloadMetaData = {}; 
                    page.assets.downloadMetaData.enabled = false;
                    var downloadFile = page.assets.dependencies.filter(function(item){
                        return ((item.associationType == 'wadl')||(item.associationType == 'swagger'));
                    })[0];
                    if(downloadFile){
                      var typeDetails = ctx.rxtManager.getRxtTypeDetails(downloadFile.associationType);
                      page.assets.downloadMetaData.enabled = true;  
                      page.assets.downloadMetaData.downloadFileType = typeDetails.singularLabel.toUpperCase();
                      pluralType = typeDetails.pluralLabel.toLowerCase();
                      page.assets.downloadMetaData.url = config.server.https+'/governance/'+pluralType+'/'+downloadFile.associationUUID+'/content?tenantId='+ctx.tenantId;          
                    }
                }
            }
        }
    };
};