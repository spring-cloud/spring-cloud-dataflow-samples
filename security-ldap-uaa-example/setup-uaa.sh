#!/bin/bash

uaac token client get admin -s adminsecret

uaac group add "dataflow.view"
uaac group add "dataflow.create"
uaac group add "dataflow.manage"

uaac group map "cn=view,ou=groups,dc=springframework,dc=org" --name="dataflow.view" --origin=ldap
uaac group map "cn=create,ou=groups,dc=springframework,dc=org" --name="dataflow.create" --origin=ldap
uaac group map "cn=manage,ou=groups,dc=springframework,dc=org" --name="dataflow.manage" --origin=ldap

uaac client add dataflow \
  --name dataflow \
  --scope cloud_controller.read,cloud_controller.write,openid,password.write,scim.userids,dataflow.view,dataflow.create,dataflow.manage \
  --authorized_grant_types password,authorization_code,client_credentials,refresh_token \
  --authorities uaa.resource \
  --redirect_uri http://localhost:9393/login \
  --autoapprove openid \
  --secret dataflow \

uaac client add skipper \
  --name skipper \
  --scope cloud_controller.read,cloud_controller.write,openid,password.write,scim.userids,dataflow.view,dataflow.create,dataflow.manage \
  --authorized_grant_types password,authorization_code,client_credentials,refresh_token \
  --authorities uaa.resource \
  --redirect_uri http://localhost:7577/login \
  --autoapprove openid \
  --secret skipper \

