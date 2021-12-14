<?php
/**
 * <p>Compute distance between two points</p>
 * @param double $lat1 point one latitute
 * @param double $lng1 point one longtitute
 * @param double $lat2 point two latitute
 * @param double $lng2 point two longtitute
 * @return double <p>distance by meter</p>
 */
function distance_two_point($lat1, $lng1, $lat2, $lng2) {
    $R = 6371;
    $d1 = deg2rad($lat1);
    $d2 = deg2rad($lat2);
    $dx = deg2rad($lat2 - $lat1);
    $dy = deg2rad($lng2 - $lng1);
    $a = sin($dx / 2) * sin($dx / 2) + cos($d1) * cos($d2) * sin($dy / 2) * sin($dy / 2);
    $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
    return $R * $c * 1000;
}

function calc_traffic($traffic_points, $line) {
    $min = PHP_INT_MAX;
    for ($i = 0; $i < count($traffic_points); $i++) {
        $b = distance_two_point($traffic_points[$i]['lat'], $traffic_points[$i]['lng'], $line->start_location->lat, $line->start_location->lng);
        $c = distance_two_point($line->start_location->lat, $line->start_location->lng, $line->end_location->lat, $line->end_location->lng);
        $a = distance_two_point($traffic_points[$i]['lat'], $traffic_points[$i]['lng'], $line->end_location->lat, $line->end_location->lng);
        $D = acos((pow($c, 2) + pow($b, 2) - pow($a, 2)) / (2 * $c * $b));
        $H = $b * sin($D);
        if ($H < $min) {
            $min = $H;
        }
    }
    return $min;
}

function get_traffic() {
    return query_database("select lat,lng from ms_TrafficPoints");
}

function get_hospitals() {
    return query_database("select name,lat,lng from ms_Hospitals");
}

function get_pollution($loc) {
    $p = query_database("Select lat,lng,pollution from ms_pollution");
    $min = PHP_INT_MAX;
    foreach ($p as $pollution) {
        $d = distance_two_point($loc[0], $loc[1], $pollution['lat'], $pollution['lng']);
        if ($d < $min) {
            $min = $d;
            $result = $pollution['pollution'];
        }
    }
    return $result;
}

function query_database($query) {
    $link = mysqli_connect('localhost', 'nameghic', 'Hfhwhgp@2518', 'nameghic_db');

    /* check connection */
    if (mysqli_connect_errno()) {
        printf("Connect failed: %s\n", mysqli_connect_error());
        exit();
    }
    /* change character set to utf8 */
    if (!mysqli_set_charset($link, "utf8")) {
        printf("Error loading character set utf8: %s\n", mysqli_error($link));
    }

    $result = mysqli_query($link, $query);
    while ($row = mysqli_fetch_assoc($result)) {
        $output[] = $row;
    }
    mysqli_close($link);
    return $output;
}
/**
 * Search hospitals by location and find nearest
 * @param string $location <p>lat,lng values with comma seperated</p>
 * @param array $hospitals <p>list of hospitals</p>
 * @return mixed <p>nearest hospitals with locations</p>
 */
function search_hospital($location, $hospitals) {
    for ($i = 0; $i < count($hospitals); $i++) {
        $hospitals[$i]['lat']=  doubleval($hospitals[$i]['lat']);
        $hospitals[$i]['lng']=  doubleval($hospitals[$i]['lng']);
        $d[] = distance_two_point($location[0], $location[1], $hospitals[$i]['lat'], $hospitals[$i]['lng']);
    }
    array_multisort($d,$hospitals);
    return $hospitals;
}

function route($from, $to, $mode) {
    $_mode = "mode=driving";

    if ($mode == 0) {
        $_mode = "mode=driving";
    } else if ($mode == 1) {
        $_mode = "mode=bicycling";
    } else if ($mode == 2) {
        $_mode = "mode=walking";
    }

    $parameters = "origin=$from&destination=$to&sensor=false&$_mode";

    $url = "https://maps.googleapis.com/maps/api/directions/json?$parameters";

    $json = file_get_contents($url);
    $obj = json_decode($json);
    $traffic_points = get_traffic();
    $disance = 0;
    $duration = 0;
    $points=array();
    for ($i = 0; $i < count($obj->routes); $i++) {
        $legs = $obj->routes[$i];
        for ($j = 0; $j < count($legs); $j++) {
            $steps = $legs->legs[$j]->steps;
            for ($k = 0; $k < count($steps); $k++) {
                $points[] = $steps[$k]->start_location;
                $points[] = $steps[$k]->end_location;
                $dis = calc_traffic($traffic_points, $steps[$k]);
                $coef = $dis < 50 ? 2 : 1;
                $duration+=$steps[$k]->duration->value * $coef;
                $disance+=$steps[$k]->distance->value;
            }
        }
    }
    return array('duration' => $duration, 'distance' => $disance, 'points' => $points);
}
