import { NT4_Client } from "../lib/NT4.js";

const toRobotPrefix = "/ShelfControls/ToRobot/";
const toDashboardPrefix = "/ShelfControls/ToDashboard/";
const advantageKitPrefix = "/AdvantageKit/RealOutputs/Objective Tracker/";

const modeTopicName = "Mode";
const carrotGoalTopicName = "CarrotGoal";
const carrotCakeGoalTopicName = "CarrotCakeGoal";

const carrotTopicName = "Carrot";
const carrotCakeTopicName = "CarrotCake";
const ovenCarrotsTopicName = "OvenCarrots";
const ovenCakesTopicName = "OvenCakes";
const bakerModeTopicName = "BakerMode";
const priorityListTopicName = "PriorityList";

const carrotScoreTargetTopicName = "Score/Carrot";
const carrotCakeScoreTargetTopicName = "Score/CarrotCake";

let mode = "SMART";
let carrotGoal = 0;
let carrotCakeGoal = 0;
let ovenCarrotsState = 0;
let ovenCakesState = 0;
let carrotState = [];
let carrotCakeState = [];
let bakerModeState = 0;
let priorityListState = [];

let carrotScoreTarget = -1;
let carrotCakeScoreTarget = -1;

const ntClient = new NT4_Client(
  window.location.hostname,
  "ShelfTracker",
  (topic) => {}, // Topic Announce
  (topic) => {}, // Topic Unannounce
  (topic, timestamp, value) => {
    if (topic.name === toDashboardPrefix + modeTopicName) {
      mode = value === 0 ? "SMART" : "DUMB";
    } else if (topic.name === toDashboardPrefix + carrotGoalTopicName) {
      carrotGoal = value;
    } else if (topic.name === toDashboardPrefix + carrotCakeGoalTopicName) {
      carrotCakeGoal = value;
    } else if (topic.name === toDashboardPrefix + carrotTopicName) {
      carrotState = convertIntToBooleanArr(value, 15);
    } else if (topic.name === toDashboardPrefix + carrotCakeTopicName) {
      carrotCakeState = convertIntToBooleanArr(value, 15);
    } else if (topic.name === toDashboardPrefix + ovenCarrotsTopicName) {
      ovenCarrotsState = value;
    } else if (topic.name === toDashboardPrefix + ovenCakesTopicName) {
      ovenCakesState = value;
    } else if (topic.name === toDashboardPrefix + bakerModeTopicName) {
      bakerModeState = value;
    } else if (topic.name === toDashboardPrefix + priorityListTopicName) {
      priorityListState = unpackInt(value, 40, 4);
    } else if (topic.name === advantageKitPrefix + carrotScoreTargetTopicName) {
      carrotScoreTarget = value;
    } else if (topic.name === advantageKitPrefix + carrotCakeScoreTargetTopicName) {
      carrotCakeScoreTarget = value;
    } else {
      return;
    }
    updateUI();
  }, // New data
  () => {
    const overlay = document.getElementById("overlay");
    if (overlay) overlay.remove();
  }, // Connect
  () => {
    if (!document.getElementById("overlay")) {
      const overlay = document.createElement("div");
      overlay.id = "overlay";

      Object.assign(overlay.style, {
        position: "fixed",
        width: "100%",
        height: "100%",
        top: "0",
        left: "0",
        right: "0",
        bottom: "0",
        backgroundColor: "rgba(0, 0, 0, 0.75)",
        zIndex: "1000",
      });

      document.body.appendChild(overlay);
    }
  } // Disconnect
);

// Start NT connection
window.addEventListener("load", () => {
  ntClient.subscribe(
    [
      toDashboardPrefix + modeTopicName,
      toDashboardPrefix + carrotGoalTopicName,
      toDashboardPrefix + carrotCakeGoalTopicName,
      toDashboardPrefix + carrotTopicName,
      toDashboardPrefix + carrotCakeTopicName,
      toDashboardPrefix + ovenCarrotsTopicName,
      toDashboardPrefix + ovenCakesTopicName,
      toDashboardPrefix + bakerModeTopicName,
      toDashboardPrefix + priorityListTopicName,
      toDashboardPrefix + carrotScoreTargetTopicName,
      toDashboardPrefix + carrotCakeScoreTargetTopicName,
    ],
    false,
    false,
    0.02
  );

  ntClient.publishTopic(toRobotPrefix + modeTopicName, "int");
  ntClient.publishTopic(toRobotPrefix + carrotGoalTopicName, "int");
  ntClient.publishTopic(toRobotPrefix + carrotCakeGoalTopicName, "int");
  ntClient.publishTopic(toRobotPrefix + carrotTopicName, "double");
  ntClient.publishTopic(toRobotPrefix + carrotCakeTopicName, "double");
  ntClient.publishTopic(toRobotPrefix + ovenCarrotsTopicName, "int");
  ntClient.publishTopic(toRobotPrefix + ovenCakesTopicName, "int");
  ntClient.publishTopic(toRobotPrefix + bakerModeTopicName, "boolean");
  ntClient.publishTopic(toRobotPrefix + priorityListTopicName, "int[]");
  ntClient.connect();
});

const ovenCarrotsDOM = document.getElementById("ovenCarrots");
const ovenCarrotsAddDOM = ovenCarrotsDOM.getElementById("add");
const ovenCarrotsCounter = ovenCarrotsDOM.getElementById("counter");
const ovenCarrotsSubtractDOM = ovenCarrotsDOM.getElementById("subtract");

const ovenCakesDOM = document.getElementById("ovenCarrots");
const ovenCakesAddDOM = ovenCakesDOM.getElementById("add");
const ovenCakesCounter = ovenCakesDOM.getElementById("counter");
const ovenCakesSubtractDOM = ovenCakesDOM.getElementById("subtract");

const bakerModeDOM = document.getElementById("baker_mode");

const modeToggleDOM = document.getElementById("mode");
const priorityListDOM = document.getElementById("priority_list");
const priorityDOM = Array.from(priorityListDOM.querySelectorAll(".priority"));
const prioritySlotDOM = priorityDOM.map((element) => element.parentElement);
const priorityItems = new Map(
  priorityDOM.map((item) => [item.dataset.idx, item])
);
const priorityUpdatedIndicated = document.querySelector(
  "#priority_list .updated"
);

const levelsDOM = Array.from(document.querySelectorAll(".level")).map((level) =>
  Array.from(level.querySelectorAll(".carrot")).map((carrot) => [
    carrot,
    carrot.querySelector(".stripe"),
  ])
);

const stockRpDOM = document.getElementById("stockRp");
const bakeRpDOM = document.getElementById("bakeRp");

function updateUI() {
  if (mode === "DUMB") {
    stockRpDOM.style.display = "none";
    bakeRpDOM.style.display = "none";
    bakerModeDOM.style.display = "none";
    priorityListDOM.style.display = "none";
  } else {
    stockRpDOM.style.display = "";
    bakeRpDOM.style.display = "";
    bakerModeDOM.style.display = "";
    priorityListDOM.style.display = "";
  }

  if (mode === "SMART" && carrotScoreTarget === -1) {
    ovenCarrotsDOM.classList.add("locked");
  } else {
    ovenCarrotsDOM.classList.remove("locked");
  }

  if (mode === "SMART" && carrotCakeScoreTarget === -1) {
    ovenCakesDOM.classList.add("locked");
  } else {
    ovenCakesDOM.classList.remove("locked");
  }

  levelsDOM.forEach((levelDOM, level) => {
    levelDOM.forEach(([carrotDOM, carrotCakeDOM], pos) => {
        let index = getCarrotID({level, pos});
        if (
            (mode === "SMART" && carrotCakeState[index]) ||
            (mode === "DUMB" && index === carrotCakeGoal) 
        ){
            carrotDOM.classList.add("selected");
            carrotCakeDOM.classList.add("selected");
        }else if (
            (mode === "SMART" && carrotState[index]) ||
            (mode === "DUMB" && index === carrotGoal)
        ) {
            carrotDOM.classList.add("selected");
            carrotCakeDOM.classList.remove("selected");
        } else {
            carrotDOM.classList.remove("selected");
            carrotCakeDOM.classList.remove("selected");
        }

        if (mode === "SMART" && index === carrotCakeScoreTarget) {
            carrotDOM.classList.add("locked");
            carrotCakeDOM.classList.add("locked");
        } else if (mode === "SMART" && index === carrotScoreTarget) {
            carrotDOM.classList.add("locked");
            carrotCakeDOM.classList.remove("locked");
        } else {
            carrotDOM.classList.remove("locked");
            carrotCakeDOM.classList.remove("locked");
        }
    })
  })

  modeToggleDOM.checked = mode === "SMART";

  priorityListState.forEach((idx, i) => {
    const item = priorityItems.get(String(idx));
    if (item) prioritySlotDOM[i].appendChild(item);
    priorityUpdatedIndicated.style.display = "";
  });

  ovenCarrotsCounter.textContent = ovenCarrotsState;
  ovenCakesCounter.textContent = ovenCakesState;

  if (mode === "SMART") {
    let stockRpLevelCount = 0;
    let bakeRpLevelCount = 0;
    for (let level = 0; level < 3; level++) {
      let count = 0;
      let cakeCount = 0;
      
      for (let i = 0; i < 5; i++) {
          count += (carrotState[getCarrotID({level, i})] || carrotCakeState[getCarrotID({level, i})])
          ? 1
          : 0;
          cakeCount += (carrotCakeState[getCarrotID({level, i})]) ? 1 : 0;
      }

      if (
        count >=
        parseInt(
          priorityDOM.find(
            (element) =>
              element.dataset.kind === "srp" &&
              element.dataset.level == level + 1
          ).dataset.count
        )
      )
        stockRpLevelCount++;
    
      if (
        cakeCount >=
        parseInt(
            priorityDOM.find(
                (element) => 
                    element.dataset.kind === "brp" &&
                    element.dataset.level == level + 1
            ).dataset.count
            )
        ) bakeRpLevelCount++;

      priorityDOM
        .filter((element) => element.dataset.level - 1 == level)
        .forEach((element) => {
          const neededCount = element.dataset.count;
          let percentage = Math.min(count / neededCount, 1);
          if (element.dataset.kind === "brp") {
            percentage = Math.min(cakeCount / neededCount, 1);
          }
          if (neededCount)
            element.style.setProperty("--percentage-complete", percentage);
          if (percentage === 1) {
            element.classList.add("complete");
          } else {
            element.classList.remove("complete");
          }
        });

      if (stockRpLevelCount >= 3) {
        stockRpDOM.classList.add("complete");
      } else {
        stockRpDOM.classList.remove("complete");
      }

      if (bakeRpLevelCount >= 3) {
        bakeRpDOM.classList.add("complete");
      } else {
        bakeRpDOM.classList.remove("complete");
      }
    }
  }

  if (bakerModeState) {
    bakerModeDOM.classList.add("selected");
  } else {
    bakerModeDOM.classList.remove("selected");
  }
}

function bind(element, callback) {
  let lastActivation = 0;
  let activate = () => {
    if (new Date().getTime() - lastActivation > 250) {
      callback();
      lastActivation = new Date().getTime();
    }
  };

  element.addEventListener("touchstart", (event) => {
    event.preventDefault();
    activate();
  });
  element.addEventListener("click", (event) => {
    event.preventDefault();
    activate();
  });
  element.addEventListener("contextmenu", (event) => {
    event.preventDefault();
    activate();
  });
}

let swaps = [];

window.addEventListener("load", () => {
  bind(modeToggleDOM, () => {
    ntClient.addSample(toRobotPrefix + modeTopicName, mode === "SMART" ? 1 : 0);
  });

  
  bind(ovenCarrotsDOM, () => {
    if (mode === "SMART") return;
    ntClient.addSample(toRobotPrefix + carrotGoalTopicName, 15);
  });

  bind(ovenCakesDOM, () => {
    if (mode === "SMART") return;
    ntClient.addSample(toRobotPrefix + carrotCakeGoalTopicName, 15);
  })

  levelsDOM.forEach((levelDOM, level) => {
    levelDOM.forEach(([carrotDOM, carrotCakeDOM], pos) => {
        bind(carrotDOM, (e) => {
            if (carrotCakeDOM.contains(e.target)) return;

            if (mode === "DUMB") {
                const id = getCarrotID({level, pos});
                ntClient.addSample(toRobotPrefix + carrotGoalTopicName, id);
                return;
            }
            const id = getCarrotID({level, pos});
            const offset = carrotState[id] ? 15 : 0;
            ntClient.addSample(toRobotPrefix + carrotTopicName, id - offset);
        });

        bind(carrotCakeDOM, () => {
            if (mode === "DUMB") {
                const id = getCarrotID({level, pos});
                ntClient.addSample(toRobotPrefix + carrotCakeGoalTopicName, id);
                return;
            }
            const id = getCarrotID({level, pos});
            const offset = carrotCakeState[id] ? 15 : 0;
            ntClient.addSample(toRobotPrefix + carrotCakeTopicName, id - offset);
        });
    });
  });

  bind(ovenCarrotsAddDOM, () => {
    if (mode === "DUMB") return;
    ntClient.addSample(toRobotPrefix + ovenCarrotsTopicName, +1);
  });
  bind(ovenCarrotsSubtractDOM, () => {
    if (mode === "DUMB") return;
    if (ovenCarrotsState > 0) {
      ntClient.addSample(toRobotPrefix + ovenCarrotsTopicName, -1);
    }
  });

  bind(ovenCakesAddDOM, () => {
    if (mode === "DUMB") return;
    ntClient.addSample(toRobotPrefix + ovenCakesTopicName, +1);
  });
  bind(ovenCakesSubtractDOM, () => {
    if (mode === "DUMB") return;
    if (ovenCakesState > 0) {
      ntClient.addSample(toRobotPrefix + ovenCakesTopicName, -1);
    }
  });

  bind(bakerModeDOM, () => {
    ntClient.addSample(toRobotPrefix + bakerModeTopicName, !bakerModeState);
  });

  const swapy = Swapy.createSwapy(priorityListDOM);
  swapy.onSwap((event) => {
    swaps.push([parseInt(event.fromSlot), parseInt(event.toSlot)]);
  });

  swapy.onSwapEnd(() => {
    const combinations = swaps.map(([a, b]) => (b > a ? 1 : -1) * (a + b));
    const usedIndices = new Set();
    const indicesToKeep = [];
    for (let i = 0; i < combinations.length; i++) {
      if (usedIndices.has(i)) continue;
      let cancelsOut = false;
      for (let j = i + 1; j < combinations.length; j++) {
        if (usedIndices.has(j)) continue;
        if (combinations[i] + combinations[j] === 0) {
          cancelsOut = true;
          usedIndices.add(i);
          usedIndices.add(j);
          break;
        }
      }
      if (!cancelsOut) indicesToKeep.push(i);
    }
    const filteredSwaps = indicesToKeep.map((index) => swaps[index]);
    if (filteredSwaps.length === 0) return;

    ntClient.addSample(
      toRobotPrefix + priorityListTopicName,
      filteredSwaps.map((swap) => packInt(swap, 4))
    );
    swaps = [];
    priorityUpdatedIndicated.style.display = "none";
  });
});

function getCarrotID({level, pos}) {
    return level * 5 + pos;
}

function getCarrot(id) {
    return {
        level: Math.floor(id / 5),
        pos: id % 5,
    };
}

function convertIntToBooleanArr(n, len) {
  if (typeof n !== "bigint") {
    n = BigInt(n);
  }
  const arr = [];
  for (let i = len - 1; i >= 0; i--) {
    arr[i] = (n & 1n) === 1n;
    n = n >> 1n;
  }
  return arr;
}

function unpackInt(n, totalBits, size) {
  let values = [];
  for (let i = 0; i < totalBits / size; i++) {
    values.unshift(n & ((1 << size) - 1));
    n >>= size;
  }
  return values;
}

function packInt(values, size) {
  let n = 0;
  for (let i = 0; i < values.length; i++) {
    n = (n << size) | values[i];
  }
  return n;
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}