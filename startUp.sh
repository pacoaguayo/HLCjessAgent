#!/bin/bash
java launcher platform &
sleep 5s

java launcher monitor localhost analizador  &
sleep 5s


java launcher stage-node localhost dpsNodeAC jess &
sleep 5s


java jade.Boot -host localhost -container -agents 'hlc400:examples.jess.HLCjessAgent( 9400 )' &
sleep 5s


