document.getElementById('chatbot-toggle').addEventListener('click', function() {
    const chatbotWindow = document.getElementById('chatbot-window');
    chatbotWindow.classList.toggle('hidden');
});

document.getElementById('chatbot-send').addEventListener('click', function() {
    const input = document.getElementById('chatbot-input');
    const message = input.value;
    if (message.trim() !== '') {
        const messages = document.getElementById('chatbot-messages');
        const newMessage = document.createElement('div');
        newMessage.textContent = message;
        messages.appendChild(newMessage);
        input.value = '';
    }
});

