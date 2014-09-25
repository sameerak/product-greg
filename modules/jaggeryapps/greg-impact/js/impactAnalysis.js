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

$(document).ready(function() {
    $('body').noisy({
        'intensity': 1,
        'size': '300',
        'opacity': 0.127,
        'fallback': '',
        'monochrome': false
    }).css('background-color', '#fff39d');

    $('#graphFrame').noisy({
        'intensity': 1,
        'size': '300',
        'opacity': 0.127,
        'fallback': '',
        'monochrome': false
    }).css('background-color', '#ddd39d');

    for (entry in legend) {
        $('#legend').append('<tr>'+
                '<td class="text-left">'+
                    entry+
                '</td>'+
                '<td class="text-left">'+
                    '<img src="images/' + legend[entry]+ '">' +
                '</td>'+
            '</tr>');
    }
});

function redraw() {
    svg.attr("transform", "translate(" + d3.event.translate + ")" + " scale(" + d3.event.scale + ")");
}

function update() {

    var nodes = flatten(root),
        links = d3.layout.tree().links(nodes);

    // Restart the force layout.
    force
        .nodes(nodes)
        .links(links)
        .start();

    // Update links.
    link = link.data(links, function(d) {
        return d.target.id;
    });

    link.exit().remove();

    link.enter().insert("line", ".node")
        .attr("class", "link")
        .attr("marker-end", function (d) {
            return "url(#arrow)"
        });

    // Update nodes.
    node = node.data(nodes, function(d) {
        return d.id;
    });

    node.exit().remove();

    var nodeEnter = node.enter().append("g")
        .attr("class", function (d) { return "node" + ('image' in d ? ' imagenode' : '') })
        .on("dblclick", doubleclick)
        .on("click", click)
        .call(force.drag);

    var circle = nodeEnter.append("circle")
        .attr("cx", -3)
        .attr("cy", 0)
        .attr("r", 15);

    node.select("circle")
        .style("fill", color);



    svg.selectAll(".imagenode")
        .attr("class", "node")
        .append("image")
        .attr("x", -12)
        .attr("y", -12)
        .attr("xlink:href", function (d) {
            var url = "images/" + d['image'] ;
            var simg = this;
            var img = new Image();
            img.onload = function () {
                d.width = this.width * imageScale;
                d.height = this.height * imageScale;
                simg.setAttribute("width", d.width);
                simg.setAttribute("height", d.height);
            }
            return img.src = url;
        });

    nodeEnter.append("image")
        .attr("x", -24)
        .attr("y", -24)
        .attr("xlink:href", function (d) {
            if( d['alertimage'] != undefined)  {
                var url = "images/" + d['alertimage'] ;
                var simg = this;
                var img = new Image();
                img.onload = function () {
                    d.width = this.width * imageScale;
                    d.height = this.height * imageScale;
                    simg.setAttribute("width", d.width);
                    simg.setAttribute("height", d.height);
                }
                return img.src = url;
            }
        });

    nodeEnter.append("text")
        .attr("dy", ".35em")
        .attr("dx", "30")
        .text(function(d) { return d.name; }) ;

}

function drawArrows() {
    var markerWidth = 10,
        markerHeight = 18,
        cRadius = 29,
        refX = cRadius + (markerWidth * 2),
        refY = 0,
        drSub = cRadius + refY;


    svg.append("svg:defs").selectAll("marker")
        .data(["arrow"])
        .enter().append("svg:marker")
        .attr("id", "arrow")
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", refX)
        .attr("refY", refY)
        .attr("markerWidth", markerWidth)
        .attr("markerHeight", markerHeight)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");

    path = svg.append("svg:g").selectAll("line")
        .attr("class", function (d) {
            return "link";
        })
        .attr("marker-end", function (d) {
            return "url(#arrow)"})
        .data(force.links())
        .enter();
}

function color(d) {

    if(d._children){
        if(d._children.length > 0){
            return   "#BD31AF";
        }
    }

    if(d.children){
        if(d.children.length > 0){
            return d.color;
        }
    }

    return d.color;
}

/*
on double click expand and retract node's children
 */
function doubleclick(d) {

    if (d.children) {
        d._children = d.children;
        d.children = null;
    } else {
        d.children = d._children;
        d._children = null;
    }
    update();
}

function click(d) {

    displayInfo(d);
}

// Returns a list of all nodes under the root.
function flatten(root) {

    var nodes = [], i = 0;

    function recurse(node) {
        if (node.children)
            node.children.forEach(recurse);
        if (!node.id)
            node.id = ++i;
        nodes.push(node);
    }

    recurse(root);
    return nodes;
}

function imageZoom(img, scale) {

    d3.select(img)
        .transition()
        .attr("width", function (d) {
            return scale * d.width;
        })
        .attr("height", function (d) {
            return scale * d.height;
        });
}

function displayInfo(resource){

    $('#name').text(resource.name);
    $('#mediaType').text(resource.mediaType);
    var linkString = '<a href = "../carbon/resources/resource.jsp?region=region3&item=resource_browser_menu&path=' +
        encodeURIComponent(resource.path) + '">' + resource.path + '</a>';
    $('#path').html(linkString) ;
    if(resource.lcState==null){
        $('#lcState').text("Not defined");
    } else{
        $('#lcState').text(resource.lcState) ;
    }
}

function tick() {

link.attr("x1", function(d) { return d.source.x; })
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) { return d.target.x; })
    .attr("y2", function(d) { return d.target.y; });

node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
}