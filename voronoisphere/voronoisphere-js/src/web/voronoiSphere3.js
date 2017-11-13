function geoPoint(lat, long, rot) {
    const rad = Math.cos(lat);
    return {
        rotation: rot,
        y: Math.sin(lat),
        r: rad,
        x: rad * Math.cos(rot + long),
        z: rad * Math.sin(rot + long)
    }
}

const size = 600;
const commandHeight = 200;

const rotationTimeInSecond = 3;
let lastAnimationTime;
const averageFps = [];

function newPoints(count) {
    const array = new Array(count).fill(undefined);
    return array.map(function () {
        return [(Math.random() * 360), (Math.random() * 360)]
    });
}

const circleRadius = d3.scaleLinear();
circleRadius.range([2, 0]);

const pointToScreen = d3.scaleLinear();
pointToScreen.domain([-1, 1]);
pointToScreen.range([0, 400]);

let voronoi;
let polygons;
// let triangles;
let sites;

let sitesPoints;
let randomPoints;

function getSites(points, rotation) {
    return points.map(function (p) {
        const gp = geoPoint(p[0], p[1], rotation);
        return [pointToScreen(gp.x) + 100, pointToScreen(gp.y) + commandHeight, circleRadius(gp.z)]
    })
}

function voronoiSphere() {

    const width = size;
    const height = size + commandHeight;

    randomPoints = newPoints(100);
    sitesPoints = getSites(randomPoints, 0);
    voronoi = d3.voronoi().extent([[-1, -1], [width + 1, height + 1]]);
    const diagram = voronoi(sitesPoints);

    const svg = d3.select("svg")
        //.append("svg")
        .attr("width", width)
        .attr("height", height)
        .append("g")
        .attr("transform", "rotate(-20, 300, 400)");

    polygons = svg.append("g")
        .attr("class", "polygons")
        .attr("clip-path", "url(#circle-mask)");

    polygons.selectAll("path")
        .data(diagram.polygons())
        .enter().append("path")
        .call(redrawPolygon);

    sites = svg.append("g").attr("class", "sites");
    sites.selectAll("circle")
        .data(sitesPoints)
        .enter().append("circle")
        .call(redrawSite);

    d3.timer(animate);
    lastAnimationTime = new Date().getTime()
}

function animate(elapsed) {
    const rotation = elapsed / (1000 * rotationTimeInSecond);

    const curFps = Math.round(1000 / (elapsed - lastAnimationTime));
    averageFps.push(curFps);
    let aveFps = 0;
    if (averageFps.length > 10) {
        averageFps.shift()
    }
    averageFps.forEach(function (d) {
        aveFps += d;
    });
    aveFps /= averageFps.length;

    lastAnimationTime = elapsed;

    if (aveFps >= 40) {
        randomPoints.push(newPoints(1)[0]);
    }

    sitesPoints = getSites(randomPoints, rotation);

    d3.select("#num").text("Number of points: " + randomPoints.length);
    d3.select("#fps").text("FPS: " + aveFps);

    redraw();
}

function redraw() {

    const diagram = voronoi(sitesPoints);

    // REMOVE
    sites.exit().remove(); /// TODO : SITES ???

    // EDIT
    polygons.selectAll("path").data(diagram.polygons()).call(redrawPolygon);

    // APPEND
    polygons.selectAll("path")
        .data(diagram.polygons())
        .enter().append("path")
        .call(redrawPolygon);

    // REMOVE
    sites.exit().remove(); /// TODO : SITES ???

    // EDIT
    sites.selectAll("circle").data(sitesPoints).call(redrawSite);

    // APPEND
    sites.selectAll("circle")
        .data(sitesPoints)
        .enter().append("circle")
        .call(redrawSite);

}

function redrawPolygon(polygon) {
    polygon
        .attr("d", function (d) {
            return d ? "M" + d.join("L") + "Z" : null;
        });
}

function redrawSite(site) {
    site
        .attr("r", function (d) {
            return d[2];
        })
        .attr("cx", function (d) {
            return d[0];
        })
        .attr("cy", function (d) {
            return d[1];
        });
}
