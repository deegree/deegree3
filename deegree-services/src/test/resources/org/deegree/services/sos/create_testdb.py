#!/usr/bin/env python

"""
create some random data and store it in the observations database
usage: ./create_inserts.py | ij
"""

import random
import datetime
one_hour = datetime.timedelta(hours=1)
start = datetime.datetime(2008, 01, 1)
time = start

print """
CONNECT 'jdbc:derby:observations;create=true';

DROP TABLE observations;

CREATE TABLE observations(
    id INT NOT NULL PRIMARY KEY,
    time TIMESTAMP NOT NULL,
    wind DOUBLE NOT NULL,
    temp DOUBLE NOT NULL,
    foo NUMERIC NOT NULL
);

CREATE INDEX idx_observations_id ON observations(id);
CREATE INDEX idx_observations_time ON observations(time);
"""

for i in range(10000):
    print "INSERT INTO observations (id, time, wind, temp, foo) VALUES(%d, TIMESTAMP('%s'), %f, %f, %d);" % \
        (i+1, time.isoformat(' '), random.random() * 10, random.random() * 30 - 5, random.random() * 100)
    time += one_hour

print """
DROP TABLE observations_small;

CREATE TABLE observations_small(
    id INT NOT NULL PRIMARY KEY,
    time TIMESTAMP NOT NULL,
    wind DOUBLE NOT NULL,
    temp DOUBLE NOT NULL,
    foo NUMERIC NOT NULL
);
"""

two_days = datetime.timedelta(days=2)
time = start

for i in range(20):
    print "INSERT INTO observations_small (id, time, wind, temp, foo) VALUES(%d, TIMESTAMP('%s'), %f, %f, %d);" % \
        (i+1, time.isoformat(' '), random.random() * 10, random.random() * 30 - 5, random.random() * 100)
    time += two_days
