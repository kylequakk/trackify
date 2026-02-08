console.log("Script is working");

/* ---------------------------------------------------------
   1. FAILSAFE LOADER (INDEX + HOMEPAGE)
--------------------------------------------------------- */
const hideLoader = () => {
    const overlay = document.getElementById("loader-overlay");
    if (!overlay) return;
    setTimeout(() => overlay.classList.add("loader-hidden"), 3500);
};

window.addEventListener("load", hideLoader);
setTimeout(hideLoader, 6000);


/* ---------------------------------------------------------
   2. SERVICE WORKER (INDEX ONLY)
--------------------------------------------------------- */
if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
        navigator.serviceWorker.register("sw.js")
            .then(() => console.log("Service Worker Registered"))
            .catch(err => console.log("Service Worker Failed:", err));
    });
}


/* ---------------------------------------------------------
   3. PARTICLE ENGINE (INDEX ONLY)
--------------------------------------------------------- */
const canvas = document.getElementById("particleCanvas");
const btn = document.getElementById("get-started-btn");

if (canvas && btn) {
    const ctx = canvas.getContext("2d");
    let particles = [];

    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }
    window.addEventListener("resize", resize);
    resize();

    class Particle {
        constructor(x, y) {
            this.x = x;
            this.y = y;
            const angle = Math.random() * Math.PI * 2;
            const velocity = Math.random() * 6 + 2;
            this.dx = Math.cos(angle) * velocity;
            this.dy = Math.sin(angle) * velocity;
            this.size = Math.random() * 4 + 1.5;
            this.alpha = 1;
            this.color = Math.random() > 0.5 ? "#0ACDFF" : "#CBE896";
        }
        draw() {
            ctx.save();
            ctx.globalAlpha = this.alpha;
            ctx.fillStyle = this.color;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
            ctx.fill();
            ctx.restore();
        }
        update() {
            this.x += this.dx;
            this.y += this.dy;
            this.dy += 0.1;
            this.alpha -= 0.025;
            this.size *= 0.95;
        }
    }

    function animate() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        particles.forEach((p, i) => {
            p.update();
            p.draw();
            if (p.alpha <= 0) particles.splice(i, 1);
        });
        requestAnimationFrame(animate);
    }
    animate();

    btn.addEventListener("click", () => {
        const rect = btn.getBoundingClientRect();
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height / 2;

        for (let i = 0; i < 22; i++) {
            particles.push(new Particle(centerX, centerY));
        }

        setTimeout(() => {
            window.location.href = "pages/loginPage.html";
        }, 400);
    });
}


/* ---------------------------------------------------------
   4. LOGIN (loginPage.html)
--------------------------------------------------------- */
async function login() {
    const email = document.getElementById("loginEmail")?.value;
    const password = document.getElementById("loginPassword")?.value;

    if (!email || !password) {
        alert("Please fill out all fields");
        return;
    }

    const response = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
    });

    const text = await response.text();
    console.log(text);

    if (text === "Login successful") {
        window.location.href = "homePage.html";
    } else {
        alert(text);
    }
}


/* ---------------------------------------------------------
   5. SIGNUP (loginPage.html)
--------------------------------------------------------- */
async function signup() {
    const email = document.getElementById("signupEmail")?.value;
    const username = document.getElementById("signupUsername")?.value;
    const password = document.getElementById("signupPassword")?.value;

    if (!email || !username || !password) {
        alert("Please fill out all fields");
        return;
    }

    const response = await fetch("http://localhost:8080/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, username, password })
    });

    const text = await response.text();
    console.log(text);
    alert(text);
}


/* ---------------------------------------------------------
   6. SCANNER (homePage.html)
--------------------------------------------------------- */
const scanBtn = document.getElementById("scanBtn");
const cameraInput = document.getElementById("cameraInput");

if (scanBtn && cameraInput) {
    scanBtn.addEventListener("click", () => cameraInput.click());

    cameraInput.addEventListener("change", () => {
        if (cameraInput.files[0]) runScan();
    });
}

function runScan() {
    const scanLine = document.getElementById("scan-line");
    const statusText = document.getElementById("status-text");
    const historyList = document.getElementById("history-list");

    if (!scanLine || !statusText || !historyList) return;

    scanBtn.disabled = true;
    scanLine.style.display = "block";
    statusText.innerText = "Analyzing meal contents...";

    setTimeout(() => {
        const p = (Math.random() * 30 + 5).toFixed(1);
        const c = (Math.random() * 60 + 10).toFixed(1);
        const cal = (Math.random() * 400 + 200).toFixed(0);

        scanLine.style.display = "none";
        statusText.innerText = "Scan Complete!";
        scanBtn.disabled = false;

        document.getElementById("cal-bar").style.width = (cal / 800) * 100 + "%";
        document.getElementById("cal-val").innerText = cal + " kcal";
        document.getElementById("p-bar").style.width = (p / 50) * 100 + "%";
        document.getElementById("p-val").innerText = p + "g";
        document.getElementById("c-bar").style.width = (c / 100) * 100 + "%";
        document.getElementById("c-val").innerText = c + "g";

        const card = document.createElement("div");
        card.className = "history-card";
        card.innerHTML = `
            <div><strong>Meal Scan</strong><br><small>${new Date().toLocaleTimeString()}</small></div>
            <div>${cal} kcal</div>
        `;
        historyList.prepend(card);
    }, 2000);
}

