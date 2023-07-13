
CREATE TABLE stops (
    stop_id NUMERIC(5),
    stop_name VARCHAR(52),
    stop_desc VARCHAR(12),
    stop_lon NUMERIC(10,2),
    stop_lat NUMERIC(10,2)

);

CREATE TABLE stop_time (
    stop_id NUMERIC(5),
    trip_id VARCHAR(20),
    arrival_time TIME,
    departure_time TIME,
    dist_traveled FLOAT

 );


 CREATE TABLE shapes (
    shape_id VARCHAR (10),
    shape_pt_lat NUMERIC(9,6),
    shape_pt_lon NUMERIC (9,6),
    shape_pt_sequence NUMERIC (2),
    shape_dist_traveled FLOAT

 );

 CREATE TABLE routes(
    route_id VARCHAR(10) ,
    route_short_name VARCHAR(5),
    from_stop VARCHAR (45),
    to_stop VARCHAR (45)

 );

 CREATE TABLE trips (
    trip_id VARCHAR (20) ,
    route_id VARCHAR (10),
    service_id VARCHAR(15),
    trip_headsign VARCHAR (32),
    shape_id VARCHAR(10)

 );

 CREATE TABLE Itineraire (
   Ligne VARCHAR(15),
   Route VARCHAR(600)
 );

 CREATE TABLE bus (
   nom_long VARCHAR(200),
   ligne VARCHAR(20)
 );



