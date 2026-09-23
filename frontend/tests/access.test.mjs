import {test} from "node:test";
import assert from "node:assert/strict";
import {can,operationsMode,staffRoles} from "../src/shared/auth/access.js";
test("unknown and signed-out users fail closed",()=> {assert.equal(can(null,"COUNTER_SELL"),false);assert.equal(can({role:"FUTURE_ROLE"},"TRIP_MANAGE"),false);});
test("counter navigation follows grants",()=>{const user={role:"COUNTER_STAFF"};assert.equal(can(user,"COUNTER_SELL"),true);assert.equal(can(user,"STAFF_MANAGE"),false);assert.deepEqual(operationsMode(user),{driver:false,counter:true});});
test("driver receives only assigned operations",()=> {assert.deepEqual(operationsMode({role:"DRIVER"}),{driver:true,counter:false});assert.equal(can({role:"DRIVER"},"PAYMENT_COLLECT_ALL"),false);});
test("staff provisioning choices exclude passenger",()=> {assert.equal(staffRoles.includes("PASSENGER"),false);assert.ok(staffRoles.includes("COUNTER_STAFF"));});
