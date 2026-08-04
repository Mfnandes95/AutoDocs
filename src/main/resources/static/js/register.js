const p = new URLSearchParams(window.location.search);
const errEl = document.getElementById('error-msg');

if (p.has('error') && errEl) {
    errEl.classList.add('visible');
    errEl.textContent = p.get('error') === 'exists'
        ? '⚠ Este e-mail já está cadastrado.'
        : '⚠ As senhas não conferem.';
}

const senhaInput = document.getElementById('senhaInput');
const fill       = document.getElementById('strengthFill');
const label      = document.getElementById('strengthLabel');

if (senhaInput && fill && label) {
    senhaInput.addEventListener('input', () => {
        const v = senhaInput.value;
        let score = 0;
        if (v.length >= 6)           score++;
        if (v.length >= 10)          score++;
        if (/[A-Z]/.test(v))         score++;
        if (/[0-9]/.test(v))         score++;
        if (/[^A-Za-z0-9]/.test(v))  score++;

        const levels = [
            { w: '0%',   c: 'transparent', t: '' },
            { w: '25%',  c: '#ff4455',     t: 'Fraca' },
            { w: '50%',  c: '#ffaa00',     t: 'Razoável' },
            { w: '75%',  c: '#00bbff',     t: 'Boa' },
            { w: '100%', c: '#00ff88',     t: 'Forte' },
        ];

        const l = levels[Math.min(score, 4)];
        fill.style.width      = l.w;
        fill.style.background = l.c;
        label.textContent     = l.t;
        label.style.color     = l.c;
    });
}

const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', e => {
        const s1 = document.getElementById('senhaInput')?.value;
        const s2 = document.getElementById('confirmSenha')?.value;
        if (s1 !== s2) {
            e.preventDefault();
            if (errEl) {
                errEl.classList.add('visible');
                errEl.textContent = '⚠ As senhas não conferem.';
            }
        }
    });
}