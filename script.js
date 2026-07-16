// JavaScript Controller for My Calculator 4906 - Premium Edition

document.addEventListener('DOMContentLoaded', () => {
    // State management
    const state = {
        expression: '',
        result: '0',
        liveResult: '',
        isDegreeMode: true,
        memoryValue: 0,
        isVibrationEnabled: true,
        currentTheme: 'm3-dark-theme',
        history: [],
        cursorPos: 0
    };

    // DOM Elements
    const expressionDisplay = document.getElementById('calculator-expression');
    const resultDisplay = document.getElementById('calculator-result');
    const liveResultDisplay = document.getElementById('live-result');
    const angleModeIndicator = document.getElementById('angle-mode-indicator');
    const memoryIndicator = document.getElementById('memory-indicator');
    const errorMessageDisplay = document.getElementById('error-message');

    // Panes & Triggers
    const historyPane = document.getElementById('history-pane');
    const settingsPane = document.getElementById('settings-pane');
    const scientificPanel = document.getElementById('scientific-panel');
    
    const toggleHistoryBtn = document.getElementById('toggle-history-btn');
    const toggleSettingsBtn = document.getElementById('toggle-settings-btn');
    const expandScientificBtn = document.getElementById('expand-scientific-btn');
    const toggleDegRadBtn = document.getElementById('toggle-deg-rad');
    
    const closeHistoryBtn = document.getElementById('close-history');
    const closeSettingsBtn = document.getElementById('close-settings');
    const clearAllHistoryBtn = document.getElementById('clear-all-history');
    const historySearchInput = document.getElementById('history-search');
    const historyListContainer = document.getElementById('history-list');
    
    const vibrationToggle = document.getElementById('vibration-toggle');
    const themeCards = document.querySelectorAll('.theme-card');

    // Load persisted configurations
    initApp();

    // Setup Event Listeners
    setupKeypadListeners();
    setupPaneListeners();
    setupThemeListeners();

    // Initialize state & stored configurations
    function initApp() {
        // Load Settings
        state.isVibrationEnabled = localStorage.getItem('calc_vibrate') !== 'false';
        vibrationToggle.checked = state.isVibrationEnabled;

        state.currentTheme = localStorage.getItem('calc_theme') || 'm3-dark-theme';
        document.body.className = state.currentTheme;
        themeCards.forEach(card => {
            if (card.dataset.theme === state.currentTheme) {
                card.classList.add('active');
            } else {
                card.classList.remove('active');
            }
        });

        state.isDegreeMode = localStorage.getItem('calc_deg_rad') !== 'false';
        updateAngleModeDisplay();

        state.memoryValue = parseFloat(localStorage.getItem('calc_memory') || '0');
        updateMemoryDisplay();

        // Load History
        state.history = JSON.parse(localStorage.getItem('calc_history') || '[]');
        renderHistory();

        // Focus display
        expressionDisplay.innerText = '';
    }

    // Handle tactile feedback / vibration
    function triggerTactileFeedback() {
        if (state.isVibrationEnabled && navigator.vibrate) {
            navigator.vibrate(15);
        }
    }

    // Scientific Panel expand/collapse (portrait)
    expandScientificBtn.addEventListener('click', () => {
        triggerTactileFeedback();
        scientificPanel.classList.toggle('collapsed');
        expandScientificBtn.classList.toggle('active');
        const isCollapsed = scientificPanel.classList.contains('collapsed');
        expandScientificBtn.querySelector('.expand-icon').textContent = isCollapsed ? 'keyboard_arrow_up' : 'keyboard_arrow_down';
    });

    // Handle Scientific Category Tabs
    const catTabs = document.querySelectorAll('.sci-category-tab');
    const catContents = document.querySelectorAll('.sci-category-content');
    const tooltipText = document.getElementById('tooltip-text');

    catTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            triggerTactileFeedback();
            const targetCat = tab.dataset.category;
            
            catTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            
            catContents.forEach(content => {
                if (content.getAttribute('id-cat') === targetCat) {
                    content.classList.add('active');
                } else {
                    content.classList.remove('active');
                }
            });

            // Update default category tooltips
            const descriptions = {
                trig: "Trigonometry: Calculates sine, cosine, and tangent. Toggle DEG/RAD mode.",
                'inv-trig': "Inverse Trig: Calculates arcsine, arccosine, and arctangent in DEG or RAD.",
                hyper: "Hyperbolic: Calculates hyperbolic sine, cosine, and tangent.",
                power: "Powers & Roots: Square, cube, xʸ powers, and square/cube roots.",
                log: "Logarithmic & Exp: Log base 10, natural log, exponential eˣ, and powers of 10.",
                comb: "Combinatorics: Calculate Permutations (nPr), Combinations (nCr), MOD, and Factorials.",
                const: "Constants: Standard mathematical constants Pi (π) and Euler's number (e)."
            };
            tooltipText.textContent = descriptions[targetCat] || '';
        });
    });

    // Tooltip hovering / focus updates for scientific keys
    const sciButtons = document.querySelectorAll('.sci-btn');
    sciButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const desc = btn.dataset.desc;
            if (desc) {
                tooltipText.textContent = desc;
            }
        });
    });

    // Basic Keypad inputs
    function setupKeypadListeners() {
        // Handle all buttons with standard 'key' class
        const keys = document.querySelectorAll('.key');
        keys.forEach(key => {
            key.addEventListener('click', () => {
                triggerTactileFeedback();
                handleInput(key.dataset.input);
            });
        });

        // Scientific buttons input
        sciButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                triggerTactileFeedback();
                const value = btn.dataset.input;
                if (value) {
                    handleInput(value);
                }
            });
        });

        // Memory Buttons
        document.getElementById('mem-clear').addEventListener('click', () => {
            triggerTactileFeedback();
            state.memoryValue = 0;
            localStorage.setItem('calc_memory', '0');
            updateMemoryDisplay();
        });

        document.getElementById('mem-recall').addEventListener('click', () => {
            triggerTactileFeedback();
            handleInput(state.memoryValue.toString());
        });

        document.getElementById('mem-add').addEventListener('click', () => {
            triggerTactileFeedback();
            try {
                const currentVal = evalExpression(state.result);
                state.memoryValue += currentVal;
                localStorage.setItem('calc_memory', state.memoryValue.toString());
                updateMemoryDisplay();
            } catch (err) {}
        });

        document.getElementById('mem-subtract').addEventListener('click', () => {
            triggerTactileFeedback();
            try {
                const currentVal = evalExpression(state.result);
                state.memoryValue -= currentVal;
                localStorage.setItem('calc_memory', state.memoryValue.toString());
                updateMemoryDisplay();
            } catch (err) {}
        });

        // Toggle DEG/RAD
        toggleDegRadBtn.addEventListener('click', () => {
            triggerTactileFeedback();
            state.isDegreeMode = !state.isDegreeMode;
            localStorage.setItem('calc_deg_rad', state.isDegreeMode.toString());
            updateAngleModeDisplay();
            recalculateLive();
        });

        // Physical Keyboard Input Support
        document.addEventListener('keydown', (e) => {
            const keyMap = {
                '0': '0', '1': '1', '2': '2', '3': '3', '4': '4',
                '5': '5', '6': '6', '7': '7', '8': '8', '9': '9',
                '+': '+', '-': '-', '*': '×', '/': '÷', '.': '.',
                '(': '(', ')': ')', '%': '%',
                'Enter': '=', '=': '=',
                'Backspace': 'delete', 'Delete': 'clear', 'Escape': 'clear'
            };
            if (keyMap[e.key]) {
                e.preventDefault();
                triggerTactileFeedback();
                handleInput(keyMap[e.key]);
            }
        });
    }

    // Toggle Sidebar Panels
    function setupPaneListeners() {
        toggleHistoryBtn.addEventListener('click', () => {
            triggerTactileFeedback();
            historyPane.classList.toggle('hidden');
        });

        closeHistoryBtn.addEventListener('click', () => {
            triggerTactileFeedback();
            historyPane.classList.add('hidden');
        });

        toggleSettingsBtn.addEventListener('click', () => {
            triggerTactileFeedback();
            settingsPane.classList.toggle('hidden');
        });

        closeSettingsBtn.addEventListener('click', () => {
            triggerTactileFeedback();
            settingsPane.classList.add('hidden');
        });

        clearAllHistoryBtn.addEventListener('click', () => {
            triggerTactileFeedback();
            if (confirm("Are you sure you want to clear all history?")) {
                state.history = [];
                localStorage.setItem('calc_history', '[]');
                renderHistory();
            }
        });

        historySearchInput.addEventListener('input', (e) => {
            renderHistory(e.target.value.trim());
        });

        vibrationToggle.addEventListener('change', (e) => {
            triggerTactileFeedback();
            state.isVibrationEnabled = e.target.checked;
            localStorage.setItem('calc_vibrate', state.isVibrationEnabled.toString());
        });
    }

    // Color Theme configuration
    function setupThemeListeners() {
        themeCards.forEach(card => {
            card.addEventListener('click', () => {
                triggerTactileFeedback();
                themeCards.forEach(c => c.classList.remove('active'));
                card.classList.add('active');

                state.currentTheme = card.dataset.theme;
                document.body.className = state.currentTheme;
                localStorage.setItem('calc_theme', state.currentTheme);
            });
        });
    }

    // Update Status elements
    function updateAngleModeDisplay() {
        angleModeIndicator.textContent = state.isDegreeMode ? 'DEG' : 'RAD';
        toggleDegRadBtn.innerHTML = `${state.isDegreeMode ? 'DEG' : 'RAD'}<span class="sub">angle mode</span>`;
    }

    function updateMemoryDisplay() {
        if (state.memoryValue !== 0) {
            memoryIndicator.classList.remove('hidden');
        } else {
            memoryIndicator.classList.add('hidden');
        }
    }

    // Expression/Calculation Parsing Engine
    function handleInput(input) {
        errorMessageDisplay.textContent = '';
        
        if (input === 'clear') {
            state.expression = '';
            state.result = '0';
            state.liveResult = '';
        } else if (input === 'delete') {
            // Delete last character or scientific function blocks
            const functions = ['sin⁻¹(', 'cos⁻¹(', 'tan⁻¹(', 'sinh(', 'cosh(', 'tanh(', 'sin(', 'cos(', 'tan(', 'log(', 'ln(', 'exp(', 'abs(', 'cbrt(', '1/('];
            let deleted = false;
            for (let fn of functions) {
                if (state.expression.endsWith(fn)) {
                    state.expression = state.expression.substring(0, state.expression.length - fn.length);
                    deleted = true;
                    break;
                }
            }
            if (!deleted && state.expression.length > 0) {
                state.expression = state.expression.slice(0, -1);
            }
            if (state.expression === '') {
                state.result = '0';
                state.liveResult = '';
            }
        } else if (input === '±') {
            // Toggle last number sign
            if (state.expression !== '') {
                const match = state.expression.match(/(-?[\d.]+)$/);
                if (match) {
                    const lastNum = match[1];
                    state.expression = state.expression.slice(0, -lastNum.length) + (lastNum.startsWith('-') ? lastNum.slice(1) : '-' + lastNum);
                } else {
                    state.expression += '-';
                }
            } else {
                state.expression = '-';
            }
        } else if (input === '=') {
            calculateFinal();
            return;
        } else {
            // Append token
            state.expression += input;
        }

        updateDisplay();
        recalculateLive();
    }

    // Update displays & auto-scroll
    function updateDisplay() {
        expressionDisplay.innerText = state.expression;
        resultDisplay.textContent = formatResult(state.result);
        
        // Horizontal auto-scroll
        expressionDisplay.scrollLeft = expressionDisplay.scrollWidth;
    }

    // Dynamic Live Result calculation during typing
    function recalculateLive() {
        if (state.expression === '') {
            liveResultDisplay.textContent = '';
            return;
        }
        try {
            const parsed = formatToEval(state.expression);
            const r = evalExpression(parsed);
            if (!isNaN(r) && isFinite(r)) {
                state.liveResult = r.toString();
                liveResultDisplay.textContent = '=' + formatResult(state.liveResult);
            } else {
                liveResultDisplay.textContent = '';
            }
        } catch (err) {
            liveResultDisplay.textContent = '';
        }
    }

    // Format display outputs beautifully
    function formatResult(val) {
        if (val === '0') return '0';
        if (val === 'Error' || val === 'Infinity' || val === '-Infinity') return val;
        
        const num = parseFloat(val);
        if (isNaN(num)) return val;

        // Exponential notation for huge/tiny values
        if (Math.abs(num) > 1e12 || (Math.abs(num) < 1e-6 && Math.abs(num) > 0)) {
            return num.toExponential(6);
        }
        
        // Clean round floating decimals
        return Number(num.toFixed(10)).toString();
    }

    // Helper functions for math evaluations
    function formatToEval(exp) {
        let f = exp.replace(/×/g, '*').replace(/÷/g, '/');
        f = f.replace(/π/g, 'Math.PI').replace(/e/g, 'Math.E');
        
        // Factorial parsing: replace matches like 5! with fact(5)
        f = f.replace(/(\d+)!/g, 'factorial($1)');

        // Math standard mapping
        f = f.replace(/sin\(/g, state.isDegreeMode ? 'sinDeg(' : 'Math.sin(');
        f = f.replace(/cos\(/g, state.isDegreeMode ? 'cosDeg(' : 'Math.cos(');
        f = f.replace(/tan\(/g, state.isDegreeMode ? 'tanDeg(' : 'Math.tan(');
        
        f = f.replace(/sin⁻¹\(/g, state.isDegreeMode ? 'asinDeg(' : 'Math.asin(');
        f = f.replace(/cos⁻¹\(/g, state.isDegreeMode ? 'acosDeg(' : 'Math.acos(');
        f = f.replace(/tan⁻¹\(/g, state.isDegreeMode ? 'atanDeg(' : 'Math.atan(');

        f = f.replace(/sinh\(/g, 'Math.sinh(');
        f = f.replace(/cosh\(/g, 'Math.cosh(');
        f = f.replace(/tanh\(/g, 'Math.tanh(');

        f = f.replace(/log\(/g, 'Math.log10(');
        f = f.replace(/ln\(/g, 'Math.log(');
        f = f.replace(/exp\(/g, 'Math.exp(');
        f = f.replace(/√\(/g, 'Math.sqrt(');
        f = f.replace(/cbrt\(/g, 'Math.cbrt(');
        f = f.replace(/abs\(/g, 'Math.abs(');
        
        // Powers support (x^y)
        f = f.replace(/\^/g, '**');

        // Permutations / Combinations supporting e.g. 5p3 or 5c3
        f = f.replace(/(\d+)p(\d+)/g, 'permutations($1,$2)');
        f = f.replace(/(\d+)c(\d+)/g, 'combinations($1,$2)');
        
        // Modulo division e.g. 10mod3 to 10%3
        f = f.replace(/mod/g, '%');

        // Random generator
        f = f.replace(/rand/g, 'Math.random()');

        return f;
    }

    // Evaluator runtime helper functions
    window.sinDeg = (x) => Math.sin(x * Math.PI / 180);
    window.cosDeg = (x) => Math.cos(x * Math.PI / 180);
    window.tanDeg = (x) => Math.tan(x * Math.PI / 180);
    
    window.asinDeg = (x) => Math.asin(x) * 180 / Math.PI;
    window.acosDeg = (x) => Math.acos(x) * 180 / Math.PI;
    window.atanDeg = (x) => Math.atan(x) * 180 / Math.PI;

    window.factorial = (n) => {
        if (n < 0) return NaN;
        if (n === 0 || n === 1) return 1;
        let r = 1;
        for (let i = 2; i <= Math.min(n, 170); i++) r *= i;
        return r;
    };

    window.permutations = (n, r) => {
        return window.factorial(n) / window.factorial(n - r);
    };

    window.combinations = (n, r) => {
        return window.factorial(n) / (window.factorial(r) * window.factorial(n - r));
    };

    // Safely evaluate expressions
    function evalExpression(formula) {
        // Auto close open parentheses
        let openBrackets = (formula.match(/\(/g) || []).length;
        let closeBrackets = (formula.match(/\)/g) || []).length;
        while (openBrackets > closeBrackets) {
            formula += ')';
            closeBrackets++;
        }

        // Clean out safe math syntax checking
        const result = Function('"use strict"; return (' + formula + ')')();
        return result;
    }

    // Handle calculation results and store in local storage
    function calculateFinal() {
        if (state.expression === '') return;
        try {
            const parsed = formatToEval(state.expression);
            const calcValue = evalExpression(parsed);

            if (isNaN(calcValue) || !isFinite(calcValue)) {
                throw new Error("Invalid output");
            }

            state.result = calcValue.toString();
            errorMessageDisplay.textContent = '';

            // Add item to history
            const item = {
                id: Date.now().toString() + Math.random().toString(36).substr(2, 5),
                expression: state.expression,
                result: state.result,
                isPinned: false,
                isStarred: false
            };
            
            state.history.unshift(item);
            localStorage.setItem('calc_history', JSON.stringify(state.history));
            renderHistory();

            // Store current expression as old, clear state live values
            state.expression = state.result;
            state.liveResult = '';
            
            updateDisplay();
            liveResultDisplay.textContent = '';
        } catch (err) {
            errorMessageDisplay.textContent = 'Invalid Formula';
            state.result = 'Error';
            updateDisplay();
        }
    }

    // Render Calculation History UI
    function renderHistory(query = '') {
        historyListContainer.innerHTML = '';
        
        let filtered = state.history;
        if (query) {
            filtered = state.history.filter(item => 
                item.expression.toLowerCase().includes(query.toLowerCase()) || 
                item.result.toLowerCase().includes(query.toLowerCase())
            );
        }

        // Sort: Pin items on top
        filtered.sort((a, b) => {
            if (a.isPinned && !b.isPinned) return -1;
            if (!a.isPinned && b.isPinned) return 1;
            return 0;
        });

        if (filtered.length === 0) {
            historyListContainer.innerHTML = '<div class="empty-history">No calculations found</div>';
            return;
        }

        filtered.forEach(item => {
            const div = document.createElement('div');
            div.className = `history-item ${item.isPinned ? 'pinned' : ''}`;
            div.innerHTML = `
                <div class="history-item-exp">${item.expression}</div>
                <div class="history-item-res">${formatResult(item.result)}</div>
                <div class="history-item-actions">
                    <button class="history-item-btn pin-btn ${item.isPinned ? 'active' : ''}" title="Pin Calculation">
                        <span class="material-symbols-outlined" style="font-size:18px">push_pin</span>
                    </button>
                    <button class="history-item-btn star-btn ${item.isStarred ? 'active' : ''}" title="Favorite">
                        <span class="material-symbols-outlined" style="font-size:18px">star</span>
                    </button>
                    <button class="history-item-btn copy-btn" title="Copy Result">
                        <span class="material-symbols-outlined" style="font-size:18px">content_copy</span>
                    </button>
                    <button class="history-item-btn delete-btn" title="Delete">
                        <span class="material-symbols-outlined" style="font-size:18px">delete</span>
                    </button>
                </div>
            `;

            // Row click triggers reuse of expression
            div.addEventListener('click', (e) => {
                if (e.target.closest('.history-item-btn')) return;
                triggerTactileFeedback();
                state.expression = item.expression;
                state.result = item.result;
                updateDisplay();
                recalculateLive();
                historyPane.classList.add('hidden');
            });

            // Action: Copy Result
            div.querySelector('.copy-btn').addEventListener('click', () => {
                triggerTactileFeedback();
                navigator.clipboard.writeText(item.result);
                alert('Copied to clipboard: ' + formatResult(item.result));
            });

            // Action: Toggle Pin
            div.querySelector('.pin-btn').addEventListener('click', () => {
                triggerTactileFeedback();
                item.isPinned = !item.isPinned;
                saveAndRenderHistory();
            });

            // Action: Toggle Star
            div.querySelector('.star-btn').addEventListener('click', () => {
                triggerTactileFeedback();
                item.isStarred = !item.isStarred;
                saveAndRenderHistory();
            });

            // Action: Delete Single item
            div.querySelector('.delete-btn').addEventListener('click', () => {
                triggerTactileFeedback();
                state.history = state.history.filter(h => h.id !== item.id);
                saveAndRenderHistory();
            });

            historyListContainer.appendChild(div);
        });
    }

    function saveAndRenderHistory() {
        localStorage.setItem('calc_history', JSON.stringify(state.history));
        renderHistory(historySearchInput.value.trim());
    }
});
