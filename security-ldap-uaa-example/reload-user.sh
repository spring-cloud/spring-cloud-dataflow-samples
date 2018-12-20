#!/bin/bash
username="$1"
password="$2"
uaac token delete --all
uaac target http://localhost:8080/uaa
uaac token owner get cf $username -s "" -p $password
uaac token client get admin -s adminsecret
uaac user get $username
