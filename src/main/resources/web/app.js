// Email Triage front-end (client-side rendered, backed by the /triage REST API).
//
// Contract (matches TriageController / EmailDataService):
//
//   GET  /triage/today  -> Summary { "statChips": [...], "categoryGroups": [ { "category": ..., "emails": [ <email>, ... ] } ] }
//   GET  /triage/weekly -> [ CategoryGroup { "category": ..., "emails": [ <email>, ... ] } ]
//   GET  /email/{id}    -> <email>
//   POST /feed-back/{id}
//        body: { "category": "<category name>" }   (DTO is FeedbackCategory(Category category))
//
// <email> = TriageEmail record:
//   { "id": 1, "sender": "...", "subject": "...", "receivedAt": "<ISO instant>",
//     "snippet": "...", "body": "...", "category": "NEEDS_REPLY",
//     "reason": "...", "draft": "..." | null }
//   receivedAt is rendered in the viewer's local timezone by fmtAt().
//
// `category` may serialize as the enum name ("NEEDS_REPLY") or as an object
// ({ "name": ..., "label": ... }) depending on Jackson config — both handled.
//
// Append ?demo to the URL to render with built-in sample data (no API calls).

const CATEGORIES = {
    NEEDS_REPLY: {label: "Needs reply"},
    FYI: {label: "FYI"},
    NEWSLETTER: {label: "Newsletter"},
    JUNK: {label: "Junk"},
};
const CAT_ORDER = Object.keys(CATEGORIES);

const DEMO = new URLSearchParams(location.search).has("demo");
const app = document.getElementById("app");

// ---------- helpers ----------

function catKey(category) {
    return typeof category === "string" ? category : (category?.name ?? "");
}

function catLabel(category) {
    return category?.label ?? CATEGORIES[catKey(category)]?.label ?? catKey(category);
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;",
    }[ch]));
}

function groupByCategory(emails) {
    const map = new Map();
    for (const e of emails ?? []) {
        const k = catKey(e.category);
        if (!map.has(k)) map.set(k, []);
        map.get(k).push(e);
    }
    return CAT_ORDER.filter(k => map.has(k)).map(k => ({category: k, emails: map.get(k)}));
}

function fmtAt(iso) {
    const d = new Date(iso);
    if (isNaN(d)) return String(iso ?? "");
    const today = new Date().toDateString() === d.toDateString();
    return today ? d.toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"})
                 : d.toLocaleString([], {month: "short", day: "numeric", hour: "2-digit", minute: "2-digit"});
}

function csrfToken() {
    const m = document.cookie.match(/(?:^|;\s*)csrf-token=([^;]*)/);
    return m ? m[1] : "";
}

async function api(path, options = {}) {
    if (DEMO) return demoApi(path, options);
    const headers = {...(options.headers ?? {})};
    if (options.method && options.method !== "GET") headers["csrf-token"] ??= csrfToken();
    const res = await fetch(path, {...options, headers});
    if (!res.ok) throw Object.assign(new Error(`HTTP ${res.status}`), {status: res.status});
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

// ---------- index view (two widgets) ----------

function emailCard(email) {
    const key = catKey(email.category);
    return `<a class="email card" data-cat="${key}" href="#/emails/${email.id}">
        <div class="top">
            <div>
                <div class="subject">${escapeHtml(email.subject)}</div>
                <div class="from muted">${escapeHtml(email.sender)}</div>
                <div class="snippet muted">${escapeHtml(email.snippet)}</div>
            </div>
            <div class="muted">${escapeHtml(fmtAt(email.receivedAt))}</div>
        </div>
        ${email.reason ? `<div class="reason">${escapeHtml(email.reason)}</div>` : ""}
        <span class="badge badge-${key}">${escapeHtml(catLabel(email.category))}</span>
    </a>`;
}

function flattenEmails(data) {
    const groups = Array.isArray(data) ? data : (data?.categoryGroups ?? []);
    return groups.flatMap(g => g?.emails ?? []);
}

function todayBody(emails) {
    const groups = groupByCategory(emails);
    if (!groups.length) return `<div class="card empty">No emails triaged yet today.</div>`;
    const stats = groups.map(g => `<div class="stat" data-filter-cat="${g.category}" title="Filter by ${escapeHtml(catLabel(g.category))}">
        <div class="n">${g.emails.length}</div>
        <span class="badge badge-${g.category}">${escapeHtml(catLabel(g.category))}</span>
    </div>`).join("");
    const list = groups.map(g => `
        <h2 class="group" data-cat="${g.category}">${escapeHtml(catLabel(g.category))} &middot; ${g.emails.length}</h2>
        ${g.emails.map(emailCard).join("")}`).join("");
    return `<div class="stats">${stats}</div>${list}`;
}

function weekBody(emails) {
    const groups = groupByCategory(emails);
    if (!groups.length) return `<div class="card empty">No emails triaged this week.</div>`;
    const total = emails.length;
    const max = Math.max(...groups.map(g => g.emails.length));
    const bars = groups.map(g => {
        const pct = Math.max(4, Math.round((g.emails.length / max) * 100));
        return `<div class="week-row" data-filter-cat="${g.category}" title="Filter by ${escapeHtml(catLabel(g.category))}">
            <span class="badge badge-${g.category}">${escapeHtml(catLabel(g.category))}</span>
            <div class="week-bar"><span class="week-fill fill-${g.category}" style="width:${pct}%"></span></div>
            <span class="week-count muted">${g.emails.length}</span>
        </div>`;
    }).join("");
    return `<div class="week-total"><span class="n">${total}</span><span class="muted"> emails this week</span></div>
        ${bars}
        <h2 class="group">Emails</h2>
        ${(emails ?? []).map(emailCard).join("")}`;
}

function bindCategoryFilters(widgetEl) {
    widgetEl.querySelectorAll("[data-filter-cat]").forEach(chip => {
        chip.addEventListener("click", () => {
            const cat = widgetEl.dataset.filter === chip.dataset.filterCat ? "" : chip.dataset.filterCat;
            widgetEl.dataset.filter = cat;
            widgetEl.querySelectorAll("[data-cat]").forEach(el => {
                el.style.display = (!cat || el.dataset.cat === cat) ? "" : "none";
            });
            widgetEl.querySelectorAll("[data-filter-cat]").forEach(c =>
                c.classList.toggle("active", c.dataset.filterCat === cat));
        });
    });
}

function widgetError(err) {
    return `<div class="card empty">${err.status === 404 ? "Not found." : `API unavailable (${escapeHtml(err.message)}).`}</div>`;
}

async function renderIndex() {
    app.innerHTML = `<div class="dashboard">
        <section class="widget" id="w-today">
            <h2 class="widget-title">Today</h2>
            <div class="widget-body"><div class="card empty">Loading&hellip;</div></div>
        </section>
        <section class="widget" id="w-week">
            <h2 class="widget-title">This week</h2>
            <div class="widget-body"><div class="card empty">Loading&hellip;</div></div>
        </section>
    </div>`;

    const [today, week] = await Promise.allSettled([api("/triage/today"), api("/triage/weekly")]);
    document.querySelector("#w-today .widget-body").innerHTML =
        today.status === "fulfilled" ? todayBody(flattenEmails(today.value)) : widgetError(today.reason);
    document.querySelector("#w-week .widget-body").innerHTML =
        week.status === "fulfilled" ? weekBody(flattenEmails(week.value)) : widgetError(week.reason);
    bindCategoryFilters(document.getElementById("w-today"));
    bindCategoryFilters(document.getElementById("w-week"));
}

// ---------- detail view ----------

async function renderDetail(id) {
    try {
        const email = await api(`/email/${id}`);
        const key = catKey(email.correctedCategory ?? email.category);
        const radios = Object.entries(CATEGORIES).map(([k, c]) => `
            <label><input type="radio" name="category" value="${k}" ${k === key ? "checked" : ""}>
            ${escapeHtml(c.label)}</label>`).join("");

        app.innerHTML = `
            <p><a href="#/">&larr; Back to dashboard</a></p>
            <div class="card">
                <div class="meta">
                    <span class="badge badge-${key}">${escapeHtml(catLabel(email.category))}</span>
                    <span class="muted">${escapeHtml(email.sender)}</span>
                    <span class="muted">&middot;</span>
                    <span class="muted">${escapeHtml(fmtAt(email.receivedAt))}</span>
                </div>
                <div class="subject">${escapeHtml(email.subject)}</div>
                <div class="body">${escapeHtml(email.body)}</div>
                <div class="reason">Why: ${escapeHtml(email.reason)}</div>
            </div>
            ${email.draft ? `<div class="card draft-card stack">
                <p class="section-label">Draft reply &mdash; suggestion only, never sent</p>
                <div class="body">${escapeHtml(email.draft)}</div>
            </div>` : ""}
            <div class="card stack">
                <p class="section-label">Correct the category</p>
                <form class="feedback" id="feedback-form">
                    ${radios}
                    <button type="submit">Save correction</button>
                </form>
                <div id="feedback-status" class="muted"></div>
            </div>`;

        document.getElementById("feedback-form").addEventListener("submit", async e => {
            e.preventDefault();
            const category = new FormData(e.target).get("category");
            const status = document.getElementById("feedback-status");
            try {
                await api(`/feed-back/${email.id}`, {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({category}),
                });
                status.textContent = "Saved.";
            } catch (err) {
                status.textContent = err.message;
            }
        });
    } catch (err) {
        app.innerHTML = `<div class="card empty">${err.status === 404 ? "Email not found." : `API unavailable (${escapeHtml(err.message)}).`}</div>`;
    }
}

// ---------- router ----------

function route() {
    const match = location.hash.match(/^#\/emails\/(\d+)/);
    if (match) renderDetail(match[1]); else renderIndex();
}

window.addEventListener("hashchange", route);
route();

// ---------- demo data (?demo) ----------

const SAMPLE_EMAILS = [
    {id: 1, sender: "Priya Nair <priya@acme.io>", subject: "Re: Launch timeline — need your sign-off",
     receivedAt: "09:12", snippet: "Can you confirm the Nov 12 date works for your team?",
     body: "Hi,\n\nCan you confirm the Nov 12 date works for your team? The vendor needs an answer by Thursday.\n\nThanks,\nPriya",
     category: "NEEDS_REPLY", reason: "Direct question addressed to you with a deadline.",
     draft: "Hi Priya,\n\nNov 12 works on our side — I'll send the signed-off timeline today.\n\nBest,\nLalith"},
    {id: 2, sender: "CI Bot <ci@build.internal>", subject: "Build #4821 failed: email.triage (main)",
     receivedAt: "08:47", snippet: "1 test failed in TriageServiceTest",
     body: "Build #4821 on branch main failed.\n\nFailed tests:\n  TriageServiceTest.classificationPromptIncludesCorrections",
     category: "FYI", reason: "Automated notification; informational, no reply expected.", draft: null},
    {id: 3, sender: "The Pragmatic Engineer <newsletter@pragmaticengineer.com>", subject: "What we learned migrating off microservices",
     receivedAt: "06:30", snippet: "This week: inside a large team's move back to a monolith...",
     body: "This week: inside a large team's move back to a monolith, plus observations on platform teams.\n\nUnsubscribe",
     category: "NEWSLETTER", reason: "Bulk mailing-list content with unsubscribe footer.", draft: null},
    {id: 4, sender: "Amrita Silva <amrita@studio.example>", subject: "Invoice #1129 is due Friday",
     receivedAt: "07:58", snippet: "Friendly reminder that invoice #1129 is due this Friday.",
     body: "Hi Lalith,\n\nFriendly reminder that invoice #1129 is due this Friday. Let me know if you need anything else.\n\nAmrita",
     category: "NEEDS_REPLY", reason: "Action required (payment) and a personal reply likely expected.",
     draft: "Hi Amrita,\n\nThanks for the reminder — I'll process #1129 before Friday.\n\nBest,\nLalith"},
    {id: 5, sender: "Deals <promo@megamart.example>", subject: "48 HOURS ONLY: extra 30% off everything",
     receivedAt: "05:15", snippet: "Don't miss out — sale ends tomorrow!",
     body: "Don't miss out — sale ends tomorrow! Shop now and save.\n\nUnsubscribe",
     category: "JUNK", reason: "Promotional blast with urgency language; no personal relevance.", draft: null},
    {id: 6, sender: "GitHub <notifications@github.com>", subject: "[email.triage] PR #14 approved",
     receivedAt: "09:40", snippet: "devin-ai-integration approved your pull request.",
     body: "devin-ai-integration approved your pull request #14.\n\nYou are receiving this because you authored the thread.",
     category: "FYI", reason: "Repository notification; informational only.", draft: null},
];

function demoApi(path) {
    if (path === "/triage/today") {
        return Promise.resolve({statChips: [], categoryGroups: groupByCategory(SAMPLE_EMAILS)});
    }
    if (path === "/triage/weekly") {
        const counts = {NEEDS_REPLY: 9, FYI: 14, NEWSLETTER: 6, JUNK: 11};
        let id = 100;
        const emails = Object.entries(counts).flatMap(([cat, n]) =>
            Array.from({length: n}, (_, i) => ({
                id: id++, sender: `${CATEGORIES[cat].label} sender`, subject: `${CATEGORIES[cat].label} sample #${i + 1}`,
                receivedAt: "—", snippet: "Weekly sample", body: "", category: cat, reason: "", draft: null,
            })));
        return Promise.resolve(groupByCategory(emails));
    }
    const emailMatch = path.match(/^\/email\/(\d+)/);
    if (emailMatch) {
        const email = SAMPLE_EMAILS.find(e => e.id === Number(emailMatch[1]));
        return email ? Promise.resolve(email)
                     : Promise.reject(Object.assign(new Error("Not found"), {status: 404}));
    }
    if (/^\/feed-back\/\d+/.test(path)) return Promise.resolve(null);
    return Promise.reject(Object.assign(new Error("Not found"), {status: 404}));
}
