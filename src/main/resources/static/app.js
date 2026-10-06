// State
let jwtToken = null;
let apiKey = null;
let userRole = null;

// DOM Elements
const loginScreen = document.getElementById('login-screen');
const dashboardScreen = document.getElementById('dashboard-screen');
const loginForm = document.getElementById('login-form');
const loginBtn = document.getElementById('login-btn');
const loginSpinner = document.getElementById('login-spinner');
const loginError = document.getElementById('login-error');
const logoutBtn = document.getElementById('logout-btn');
const userRoleBadge = document.getElementById('user-role');

const chatForm = document.getElementById('chat-form');
const chatInput = document.getElementById('chat-input');
const chatHistory = document.getElementById('chat-history');
const chatBtn = document.getElementById('chat-btn');
const chatSpinner = document.getElementById('chat-spinner');

const statRequests = document.getElementById('stat-requests');
const statTokens = document.getElementById('stat-tokens');
const statCost = document.getElementById('stat-cost');

// Helper to decode JWT
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return null;
    }
}

// Authentication
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    loginError.textContent = '';
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    // UI state
    loginBtn.querySelector('span').style.display = 'none';
    loginSpinner.style.display = 'inline-block';
    
    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (!res.ok) {
            throw new Error('Invalid username or password');
        }
        
        const data = await res.json();
        jwtToken = data.token;
        
        // Decode token to get role and apiKey
        const claims = parseJwt(jwtToken);
        if (claims) {
            apiKey = claims.apiKey;
            userRole = claims.role;
            userRoleBadge.textContent = userRole;
            
            // Switch screens
            loginScreen.style.display = 'none';
            dashboardScreen.style.display = 'block';
            
            // Fetch initial stats
            fetchStats();
        }
    } catch (err) {
        loginError.textContent = err.message;
    } finally {
        loginBtn.querySelector('span').style.display = 'inline';
        loginSpinner.style.display = 'none';
    }
});

logoutBtn.addEventListener('click', () => {
    jwtToken = null;
    apiKey = null;
    userRole = null;
    loginScreen.style.display = 'flex';
    dashboardScreen.style.display = 'none';
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
    chatHistory.innerHTML = '<div class="message bot">System: Connected to LLM Gateway. Ready to route your prompts.</div>';
});

// Chat functionality
chatForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const prompt = chatInput.value.trim();
    if (!prompt) return;
    
    // Add user message to UI
    appendMessage(prompt, 'user');
    chatInput.value = '';
    
    // UI state
    chatBtn.querySelector('span').style.display = 'none';
    chatSpinner.style.display = 'inline-block';
    
    try {
        const res = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                apiKey: apiKey,
                prompt: prompt,
                model: "qwen/qwen3.8-27b" // Defaulting to Groq model for this demo
            })
        });
        
        const data = await res.json();
        
        if (!res.ok) {
            appendMessage(`Error: ${data.message || data.error || 'Request failed'}`, 'bot');
        } else {
            appendMessage(data.content, 'bot', `${data.providerUsed} | ${data.promptTokens + data.completionTokens} tokens`);
            // Refresh stats after successful chat
            fetchStats();
        }
    } catch (err) {
        appendMessage(`System Error: ${err.message}`, 'bot');
    } finally {
        chatBtn.querySelector('span').style.display = 'inline';
        chatSpinner.style.display = 'none';
    }
});

function appendMessage(text, sender, metaText = '') {
    const div = document.createElement('div');
    div.className = `message ${sender}`;
    // Replace newlines with <br> for HTML display
    div.innerHTML = text.replace(/\n/g, '<br>');
    
    if (metaText) {
        const meta = document.createElement('div');
        meta.className = 'message-meta';
        meta.textContent = metaText;
        div.appendChild(meta);
    }
    
    chatHistory.appendChild(div);
    chatHistory.scrollTop = chatHistory.scrollHeight;
}

// Analytics State
let providerChartInstance = null;

// Fetch Analytics
async function fetchStats() {
    try {
        let url = `/api/usage/${apiKey}`;
        if (userRole === 'ADMIN') {
            url = '/api/usage/summary';
        }
        
        const res = await fetch(url, {
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
        
        if (res.ok) {
            const data = await res.json();
            
            statRequests.textContent = data.totalRequests;
            
            if (userRole === 'ADMIN') {
                statCost.textContent = `$${data.totalCostUsd.toFixed(4)}`;
                statTokens.textContent = 'N/A (Global)'; // Summary endpoint doesn't return total tokens in current impl
                
                // Render Chart
                if (data.providerBreakdown) {
                    document.getElementById('chart-panel').style.display = 'block';
                    renderChart(data.providerBreakdown);
                }
            } else {
                statCost.textContent = `$${data.totalCostUsd.toFixed(4)}`;
                statTokens.textContent = data.totalInputTokens + data.totalOutputTokens;
                document.getElementById('chart-panel').style.display = 'none';
            }
        }
    } catch (err) {
        console.error("Failed to fetch stats", err);
    }
}

function renderChart(breakdown) {
    const ctx = document.getElementById('providerChart').getContext('2d');
    
    const labels = Object.keys(breakdown);
    const dataPoints = Object.values(breakdown).map(v => v.count);
    
    if (providerChartInstance) {
        providerChartInstance.data.labels = labels;
        providerChartInstance.data.datasets[0].data = dataPoints;
        providerChartInstance.update();
        return;
    }
    
    // Create new chart
    providerChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: dataPoints,
                backgroundColor: [
                    'rgba(99, 102, 241, 0.8)',
                    'rgba(168, 85, 247, 0.8)',
                    'rgba(16, 185, 129, 0.8)'
                ],
                borderColor: 'rgba(24, 24, 27, 1)',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { color: '#f8fafc' }
                }
            },
            cutout: '70%'
        }
    });
}
