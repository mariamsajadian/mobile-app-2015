<?php

header('Content-Type: application/json; charset=utf-8');
include_once 'msfunc.php';

if (!isset($_GET['loc']))
{
    if(isset($_GET['from']) && isset($_GET['to'])){
        $route[]=  route($_GET['from'], $_GET['to'], 0);
        echo json_encode($route, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
        exit();
    }
}
$location = $_GET['loc'];

$pollution = get_pollution(explode(',', $location));
if (!isset($_GET['mode'])) {
    //just print pollution and exit
    echo $pollution;
    exit();
}
$mode = $_GET['mode'];
if ($pollution < 100)
    exit();
//hazard mode and search hospitals

$hospitals = get_hospitals();
$hospital = search_hospital(explode(',', $location), $hospitals);
$r1 = route($location, $hospital[0]['lat'] . ',' . $hospital[0]['lng'], $mode);
$r2 = route($location, $hospital[1]['lat'] . ',' . $hospital[1]['lng'], $mode);
if ($r1['duration'] < $r2['duration']) {
    $hospital[0]+=$r1;
} else {
    $hospital[1]+=$r2;
}
echo json_encode($hospital, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);

