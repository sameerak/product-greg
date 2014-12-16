
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

function generateGraphData(registry, resourcePath, associationType){
    var util = require('/extensions/app/greg_impact/modules/utility.js');
    var log = new Log();
    log.info(util);
    var governanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
    var artifact = governanceUtils.retrieveGovernanceArtifactByPath(
        registry.registry,resourcePath);
    if (artifact){
        var graphDataObject = new Object();

        var artifactName= artifact.getAttributes("overview_name");
        /*
         If the artifact's overview_name is not exist, resource name is extracted
         from the resourcePath provided.
         */
        graphDataObject.name = (artifactName == null) ?
            resourcePath.substring(resourcePath.lastIndexOf("/") + 1) :
            artifactName[0];

        var mediaType;
        if (util.isNotNullOrEmpty(artifact.mediaType)){
            mediaType = artifact.mediaType;
        } else {
            /*
            There is only one type of artifacts that do not have media type attribute.
            These are created to represent endpoint related to services and RXTs.
            They are not created by the endpoint RXT.
             */
            mediaType = "application/vnd.wso2.endpoint";
        }

        //graphDataObject.image = util.getImageByMediaType(mediaType);
        graphDataObject.color = '#A3EA69'; //TODO implement util.getColorByMediaType(mediaType);
        graphDataObject.mediaType = mediaType;
        graphDataObject.lcState = artifact.lifecycleState;
        graphDataObject.path = artifact.path;
        graphDataObject.children = [];

        var associations = registry.associations(resourcePath);
        for (var i = 0; i < associations.length; i++) {
            if (associations[i].type == associationType) {
                if (util.isNotNullOrEmpty(associations[i].src)){
                    if (associations[i].src == resourcePath){
                        var resourceDest = associations[i].dest;
                        var targetGraphDataObject = generateGraphData(registry, resourceDest, associationType);

                        if (targetGraphDataObject) {
                            graphDataObject.children.push(targetGraphDataObject);
                        }
                    }
                }
            }
        }

        return graphDataObject;
    }
    return false;
}

/*
graph is an json object having to attributes nodes and edges
 */
function getNodesAndEdges(registry,resourcePath, graph){
    var util = require('/extensions/app/greg_impact/modules/utility.js');
    var governanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
    var artifact = governanceUtils.retrieveGovernanceArtifactByPath(
        registry.registry,resourcePath);
    if (artifact){
        var graphDataObject = new Object();

        if (graph.nodes[artifact.path]){
            graphDataObject = graph.nodes[artifact.path]
        }
        else{
            var artifactName= artifact.getAttributes("overview_name");;
            /*
             If the artifact's overview_name is not exist, resource name is extracted
             from the resourcePath provided.
             */
            graphDataObject.name = (artifactName == null) ?
                resourcePath.substring(resourcePath.lastIndexOf("/") + 1) :
                artifactName[0];

            var mediaType;
            //log.info(artifact.mediaType);
            if (util.isNotNullOrEmpty(artifact.mediaType)){
                mediaType = artifact.mediaType;
            } else {
                /*
                 There is only one type of artifacts that do not have media type attribute.
                 These are created to represent endpoint related to services and RXTs.
                 They are not created by the endpoint RXT.
                 */
                mediaType = "application/vnd.wso2.endpoint";
            }

            //graphDataObject.image = util.getImageByMediaType(mediaType);
            graphDataObject.color = (graph.index == 0) ?
                '#B17488' :
                '#A3EA69'; //TODO implement util.getColorByMediaType(mediaType);
            graphDataObject.mediaType = mediaType;
            //graphDataObject.lcState = artifact.lifecycleState;
            graphDataObject.path = artifact.path;
            graphDataObject.relations = [];
            graphDataObject.id = graph.index;
            graph.index++;

            graph.nodes[artifact.path] = graphDataObject;
            graph.nodes.push(graphDataObject);
            //log.info(graph.nodes);


            var associations = registry.associations(resourcePath);
            for (var i = 0; i < associations.length; i++) {
                if (util.isNotNullOrEmpty(associations[i].src)){
                    if (associations[i].src == resourcePath){
                        var resourceDest = associations[i].dest; // {"source":1,"target":0,"value":1},

                        if(getNodesAndEdges(registry, resourceDest, graph)){
                            
                            var relation = new Object();
                             relation.source = graphDataObject.id;
                            //log.info("destination = " + resourceDest);
                            //log.info(graph.nodes[resourceDest]);
                            relation.target = graph.nodes[resourceDest].id;
                            relation.relation = associations[i].type;
                            relation.id = graph.relationIndex;

                            graph.relations.push(relation);

                            graphDataObject.relations.push(relation.id);

                            var edge;

                            if (graph.edges[graphDataObject.id + "," + graph.nodes[resourceDest].id]){
                                log.info("edge exists " + graphDataObject.id + "," + graph.nodes[resourceDest].id);

                                edge = graph.edges[graphDataObject.id + "," + graph.nodes[resourceDest].id];
                                edge.relations.push(relation.id);

                            }
                            else{
                                
                                edge = new Object();
                                edge.source = graphDataObject.id;
                                //log.info("destination = " + resourceDest);
                                //log.info(graph.nodes[resourceDest]);
                                edge.target = graph.nodes[resourceDest].id;
                                edge.relations = [];
                                edge.relations.push(relation.id);
                                edge.value = 1;

                                graph.edges[graphDataObject.id + "," + graph.nodes[resourceDest].id] = edge;
                                graph.edges[graph.nodes[resourceDest].id + "," + graphDataObject.id] = edge;

                                //graph.edges[]
                                graph.edges.push(edge);
                            }

                            graph.relationIndex++;
                        }


                        /*if(!graph.nodes[resourceDest]){
                            getNodesAndEdges(registry, resourceDest, graph);
                        }*/
                    }
                }
            }
        }

        return true;

    }
    return false;
}
