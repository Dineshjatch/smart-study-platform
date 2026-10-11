// const API_BASE_URL = "http://localhost:8080";
const API_BASE_URL = "";

const loginSection = document.getElementById("loginSection");
const registerSection = document.getElementById("registerSection");
const dashboard = document.getElementById("dashboard");
const subjectsSection = document.getElementById("subjects");
const studySection = document.getElementById("study");
const quizzesSection = document.getElementById("quizzes");

const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");
const logoutButton = document.getElementById("logoutButton");

const showRegisterButton =
    document.getElementById("showRegisterButton");

const showLoginButton =
    document.getElementById("showLoginButton");

const loginMessage =
    document.getElementById("loginMessage");

const registerMessage =
    document.getElementById("registerMessage");

const welcomeMessage =
    document.getElementById("welcomeMessage");

const totalStudyTime =
    document.getElementById("totalStudyTime");

const totalSessions =
    document.getElementById("totalSessions");

const averageSession =
    document.getElementById("averageSession");

const subjectsList =
    document.getElementById("subjectsList");

const subjectForm =
    document.getElementById("subjectForm");

const subjectMessage =
    document.getElementById("subjectMessage");

const studyStartForm =
    document.getElementById("studyStartForm");

const studySubjectId =
    document.getElementById("studySubjectId");

const stopStudyButton =
    document.getElementById("stopStudyButton");

const studyMessage =
    document.getElementById("studyMessage");

const studyHistoryList =
    document.getElementById("studyHistoryList");

const quizForm =
    document.getElementById("quizForm");

const quizMessage =
    document.getElementById("quizMessage");

const aiQuizForm =
    document.getElementById("aiQuizForm");

const aiQuizMessage =
    document.getElementById("aiQuizMessage");

const quizzesList =
    document.getElementById("quizzesList");

const quizDetails =
    document.getElementById("quizDetails");

const quizDetailsTitle =
    document.getElementById("quizDetailsTitle");

const quizDetailsDescription =
    document.getElementById("quizDetailsDescription");

const quizQuestionsList =
    document.getElementById("quizQuestionsList");

const submitQuizButton =
    document.getElementById("submitQuizButton");

const quizResultMessage =
    document.getElementById("quizResultMessage");

const quizAttemptsList =
    document.getElementById("quizAttemptsList");


let selectedQuiz = null;


/* =========================================
   Application startup
   ========================================= */

document.addEventListener("DOMContentLoaded", () => {

    const token = localStorage.getItem("token");

    if (token) {
        showAuthenticatedPage();
        loadDashboard();
    } else {
        showLoginPage();
    }
});


/* =========================================
   Login
   ========================================= */

loginForm.addEventListener("submit", async (event) => {

    event.preventDefault();

    const email =
        document.getElementById("loginEmail")
            .value
            .trim();

    const password =
        document.getElementById("loginPassword")
            .value;

    loginMessage.textContent = "Logging in...";

    try {

        const data = await apiRequest(
            "/api/users/login",
            {
                method: "POST",
                body: {
                    email: email,
                    password: password
                },
                includeAuth: false
            }
        );

        localStorage.setItem("token", data.token);

        loginForm.reset();
        loginMessage.textContent = "";

        showAuthenticatedPage();

        await loadDashboard();

    } catch (error) {

        loginMessage.textContent =
            error.message;
    }
});


/* =========================================
   Registration
   ========================================= */

registerForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const name =
            document.getElementById("registerName")
                .value
                .trim();

        const email =
            document.getElementById("registerEmail")
                .value
                .trim();

        const password =
            document.getElementById("registerPassword")
                .value;

        registerMessage.textContent =
            "Creating account...";

        try {

            await apiRequest(
                "/api/users/register",
                {
                    method: "POST",
                    body: {
                        name: name,
                        email: email,
                        password: password
                    },
                    includeAuth: false
                }
            );

            registerForm.reset();

            registerMessage.textContent =
                "Registration successful. You can now log in.";

        } catch (error) {

            registerMessage.textContent =
                error.message;
        }
    }
);


/* =========================================
   Authentication screen switching
   ========================================= */

showRegisterButton.addEventListener(
    "click",
    () => {

        loginSection.classList.add("hidden");
        registerSection.classList.remove("hidden");

        loginMessage.textContent = "";
        registerMessage.textContent = "";
    }
);


showLoginButton.addEventListener(
    "click",
    () => {

        registerSection.classList.add("hidden");
        loginSection.classList.remove("hidden");

        loginMessage.textContent = "";
        registerMessage.textContent = "";
    }
);


/* =========================================
   Logout
   ========================================= */

logoutButton.addEventListener("click", () => {

    localStorage.removeItem("token");

    selectedQuiz = null;

    quizDetails.classList.add("hidden");

    showLoginPage();
});


/* =========================================
   Dashboard
   ========================================= */

async function loadDashboard() {

    try {

        const profile =
            await apiRequest(
                "/api/users/profile"
            );

        welcomeMessage.textContent =
            `Welcome, ${profile.name}! 👋`;

        await Promise.all([
            loadStudyStats(),
            loadSubjects(),
            loadStudyHistory(),
            loadQuizzes(),
            loadAllQuizAttempts()
        ]);

    } catch (error) {

        console.error(
            "Dashboard loading failed:",
            error
        );

        if (error.status === 401) {

            localStorage.removeItem("token");
            showLoginPage();
        }
    }
}


/* =========================================
   Study statistics
   ========================================= */

async function loadStudyStats() {

    const stats =
        await apiRequest(
            "/api/study-stats"
        );

    totalStudyTime.textContent =
        formatDuration(
            stats.totalStudySeconds
        );

    totalSessions.textContent =
        stats.totalSessions;

    averageSession.textContent =
        formatDuration(
            stats.averageSessionSeconds
        );
}


/* =========================================
   Subjects
   ========================================= */

async function loadSubjects() {

    const subjects =
        await apiRequest(
            "/api/subjects"
        );

    subjectsList.innerHTML = "";

    studySubjectId.innerHTML = `
        <option value="">
            Select a subject
        </option>
    `;

    if (subjects.length === 0) {

        subjectsList.innerHTML =
            "<p>No subjects found.</p>";

        return;
    }

    subjects.forEach((subject) => {

        const item =
            document.createElement("div");

        item.className = "list-item";

        const title =
            document.createElement("h3");

        title.textContent =
            subject.name;

        const description =
            document.createElement("p");

        description.textContent =
            subject.description ||
            "No description";

        item.appendChild(title);
        item.appendChild(description);

        subjectsList.appendChild(item);


        const option =
            document.createElement("option");

        option.value = subject.id;
        option.textContent = subject.name;

        studySubjectId.appendChild(option);
    });
}


/* =========================================
   Create subject
   ========================================= */

subjectForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const name =
            document.getElementById("subjectName")
                .value
                .trim();

        const description =
            document.getElementById("subjectDescription")
                .value
                .trim();

        subjectMessage.textContent =
            "Creating subject...";

        try {

            await apiRequest(
                "/api/subjects",
                {
                    method: "POST",
                    body: {
                        name: name,
                        description: description
                    }
                }
            );

            subjectForm.reset();

            subjectMessage.textContent =
                "Subject created successfully.";

            await loadSubjects();

        } catch (error) {

            subjectMessage.textContent =
                error.message;
        }
    }
);


/* =========================================
   Study session
   ========================================= */

studyStartForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const subjectId =
            Number(
                studySubjectId.value
            );

        if (!subjectId) {

            studyMessage.textContent =
                "Please select a subject.";

            return;
        }

        studyMessage.textContent =
            "Starting study session...";

        try {

            await apiRequest(
                "/api/study-sessions/start",
                {
                    method: "POST",
                    body: {
                        subjectId: subjectId
                    }
                }
            );

            studyMessage.textContent =
                "Study session started. 📚";

            await loadStudyStats();
            await loadStudyHistory();

        } catch (error) {

            studyMessage.textContent =
                error.message;
        }
    }
);


stopStudyButton.addEventListener(
    "click",
    async () => {

        studyMessage.textContent =
            "Stopping study session...";

        try {

            const result =
                await apiRequest(
                    "/api/study-sessions/stop",
                    {
                        method: "POST"
                    }
                );

            studyMessage.textContent =
                `Session completed: ${formatDuration(
                    result.durationSeconds
                )}`;

            await loadStudyStats();
            await loadStudyHistory();

        } catch (error) {

            studyMessage.textContent =
                error.message;
        }
    }
);


/* =========================================
   Study history
   ========================================= */

async function loadStudyHistory() {

    const history =
        await apiRequest(
            "/api/study-sessions/history"
        );

    studyHistoryList.innerHTML = "";

    if (history.length === 0) {

        studyHistoryList.innerHTML =
            "<p>No study sessions yet.</p>";

        return;
    }

    history.forEach((session) => {

        const item =
            document.createElement("div");

        item.className = "list-item";

        const title =
            document.createElement("h3");

        title.textContent =
            `Subject ID: ${session.subjectId}`;

        const details =
            document.createElement("p");

        details.textContent =
            `Duration: ${formatDuration(
                session.durationSeconds
            )}`;

        item.appendChild(title);
        item.appendChild(details);

        studyHistoryList.appendChild(item);
    });
}


/* =========================================
   Manual quiz creation
   ========================================= */

quizForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const title =
            document.getElementById("quizTitle")
                .value
                .trim();

        const description =
            document.getElementById("quizDescription")
                .value
                .trim();

        quizMessage.textContent =
            "Creating quiz...";

        try {

            await apiRequest(
                "/api/quizzes",
                {
                    method: "POST",
                    body: {
                        title: title,
                        description: description
                    }
                }
            );

            quizForm.reset();

            quizMessage.textContent =
                "Quiz created successfully.";

            await loadQuizzes();

        } catch (error) {

            quizMessage.textContent =
                error.message;
        }
    }
);


/* =========================================
   AI quiz generation
   ========================================= */

aiQuizForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const topic =
            document.getElementById("aiTopic")
                .value
                .trim();

        const numberOfQuestions =
            Number(
                document.getElementById(
                    "aiQuestionCount"
                ).value
            );

        const difficulty =
            document.getElementById(
                "aiDifficulty"
            ).value;

        aiQuizMessage.textContent =
            "Generating AI quiz... 🤖";

        try {

            const quiz =
                await apiRequest(
                    "/api/ai-quizzes/generate",
                    {
                        method: "POST",
                        body: {
                            topic: topic,
                            numberOfQuestions:
                                numberOfQuestions,
                            difficulty: difficulty
                        }
                    }
                );

            aiQuizForm.reset();

            document.getElementById(
                "aiQuestionCount"
            ).value = 5;

            document.getElementById(
                "aiDifficulty"
            ).value = "medium";

            aiQuizMessage.textContent =
                `AI quiz "${quiz.title}" created successfully.`;

            await loadQuizzes();

            await openQuiz(
                quiz.quizId
            );

        } catch (error) {

            aiQuizMessage.textContent =
                error.message;
        }
    }
);


/* =========================================
   Quiz list
   ========================================= */

async function loadQuizzes() {

    const quizzes =
        await apiRequest(
            "/api/quizzes"
        );

    quizzesList.innerHTML = "";

    if (quizzes.length === 0) {

        quizzesList.innerHTML =
            "<p>No quizzes found.</p>";

        return;
    }

    quizzes.forEach((quiz) => {

        const item =
            document.createElement("div");

        item.className = "list-item";

        const title =
            document.createElement("h3");

        title.textContent =
            quiz.title;

        const description =
            document.createElement("p");

        description.textContent =
            quiz.description ||
            "No description";

        const badge =
            document.createElement("span");

        badge.className = "badge";

        badge.textContent =
            quiz.aiGenerated
                ? "AI Generated"
                : "Manual Quiz";

        const viewButton =
            document.createElement("button");

        viewButton.type = "button";
        viewButton.textContent =
            "Open Quiz";

        viewButton.addEventListener(
            "click",
            () => openQuiz(quiz.id)
        );

        item.appendChild(title);
        item.appendChild(description);
        item.appendChild(badge);
        item.appendChild(viewButton);

        quizzesList.appendChild(item);
    });
}


/* =========================================
   Open quiz
   ========================================= */

async function openQuiz(quizId) {

    quizResultMessage.textContent =
        "";

    const quiz =
        await apiRequest(
            `/api/quizzes/${quizId}`
        );

    const questions =
        await apiRequest(
            `/api/quizzes/${quizId}/questions`
        );

    selectedQuiz = {
        quiz: quiz,
        questions: questions
    };

    quizDetailsTitle.textContent =
        quiz.title;

    quizDetailsDescription.textContent =
        quiz.description ||
        "";

    quizQuestionsList.innerHTML = "";

    if (questions.length === 0) {

        quizQuestionsList.innerHTML =
            "<p>This quiz has no questions yet.</p>";

        submitQuizButton.classList.add("hidden");
        quizDetails.classList.remove("hidden");

        return;
    }

    submitQuizButton.classList.remove("hidden");

    questions.forEach(
        (question, index) => {

            const questionContainer =
                document.createElement("div");

            questionContainer.className =
                "list-item";

            const questionTitle =
                document.createElement("h3");

            questionTitle.textContent =
                `${index + 1}. ${question.questionText}`;

            questionContainer.appendChild(
                questionTitle
            );

            const options = [
                ["A", question.optionA],
                ["B", question.optionB],
                ["C", question.optionC],
                ["D", question.optionD]
            ];

            options.forEach(
                ([letter, text]) => {

                    const label =
                        document.createElement("label");

                    const radio =
                        document.createElement("input");

                    radio.type = "radio";
                    radio.name =
                        `question-${question.id}`;
                    radio.value = letter;

                    label.appendChild(radio);

                    label.appendChild(
                        document.createTextNode(
                            ` ${letter}. ${text}`
                        )
                    );

                    questionContainer.appendChild(
                        label
                    );
                }
            );

            quizQuestionsList.appendChild(
                questionContainer
            );
        }
    );

    quizDetails.classList.remove("hidden");

    quizDetails.scrollIntoView({
        behavior: "smooth"
    });
}


/* =========================================
   Submit quiz
   ========================================= */

submitQuizButton.addEventListener(
    "click",
    async () => {

        if (!selectedQuiz) {

            quizResultMessage.textContent =
                "Please select a quiz first.";

            return;
        }

        const answers = [];

        for (const question
            of selectedQuiz.questions) {

            const selectedOption =
                document.querySelector(
                    `input[name="question-${question.id}"]:checked`
                );

            if (!selectedOption) {

                quizResultMessage.textContent =
                    "Please answer every question.";

                return;
            }

            answers.push({
                questionId: question.id,
                selectedAnswer:
                    selectedOption.value
            });
        }

        quizResultMessage.textContent =
            "Submitting quiz...";

        try {

            const result =
                await apiRequest(
                    "/api/quiz-submissions",
                    {
                        method: "POST",
                        body: {
                            quizId:
                                selectedQuiz.quiz.id,
                            answers: answers
                        }
                    }
                );

            quizResultMessage.textContent =
                `Result: ${result.score}/${result.totalQuestions} ` +
                `(${result.percentage}%) 🎉`;

            await loadAllQuizAttempts();

        } catch (error) {

            quizResultMessage.textContent =
                error.message;
        }
    }
);


/* =========================================
   Quiz attempt history
   ========================================= */

async function loadAllQuizAttempts() {

    const attempts =
        await apiRequest(
            "/api/quiz-attempts"
        );

    quizAttemptsList.innerHTML = "";

    if (attempts.length === 0) {

        quizAttemptsList.innerHTML =
            "<p>No quiz attempts yet.</p>";

        return;
    }

    attempts.forEach((attempt) => {

        const item =
            document.createElement("div");

        item.className = "list-item";

        const title =
            document.createElement("h3");

        title.textContent =
            `Quiz ${attempt.quizId}`;

        const details =
            document.createElement("p");

        details.textContent =
            `Score: ${attempt.score}/` +
            `${attempt.totalQuestions} ` +
            `(${attempt.percentage}%)`;

        item.appendChild(title);
        item.appendChild(details);

        quizAttemptsList.appendChild(item);
    });
}


/* =========================================
   Generic API helper
   ========================================= */

async function apiRequest(
    path,
    options = {}
) {

    const {
        method = "GET",
        body = null,
        includeAuth = true
    } = options;

    const headers = {};

    if (body !== null) {

        headers["Content-Type"] =
            "application/json";
    }

    if (includeAuth) {

        const token =
            localStorage.getItem("token");

        if (token) {

            headers["Authorization"] =
                `Bearer ${token}`;
        }
    }

    const fetchOptions = {
        method: method,
        headers: headers
    };

    if (body !== null) {

        fetchOptions.body =
            JSON.stringify(body);
    }

    const response =
        await fetch(
            `${API_BASE_URL}${path}`,
            fetchOptions
        );

    const contentType =
        response.headers.get(
            "content-type"
        );

    let data = null;

    if (
        contentType &&
        contentType.includes("application/json")
    ) {

        data = await response.json();

    } else {

        const text =
            await response.text();

        data = text || null;
    }

    if (!response.ok) {

        const errorMessage =
            data &&
            typeof data === "object" &&
            data.message
                ? data.message
                : `Request failed with status ${response.status}`;

        const error =
            new Error(errorMessage);

        error.status =
            response.status;

        throw error;
    }

    return data;
}


/* =========================================
   UI helpers
   ========================================= */

function showLoginPage() {

    loginSection.classList.remove("hidden");
    registerSection.classList.add("hidden");

    dashboard.classList.add("hidden");
    subjectsSection.classList.add("hidden");
    studySection.classList.add("hidden");
    quizzesSection.classList.add("hidden");
}


function showAuthenticatedPage() {

    loginSection.classList.add("hidden");
    registerSection.classList.add("hidden");

    dashboard.classList.remove("hidden");
    subjectsSection.classList.remove("hidden");
    studySection.classList.remove("hidden");
    quizzesSection.classList.remove("hidden");
}


function formatDuration(seconds) {

    if (!seconds || seconds <= 0) {
        return "0 min";
    }

    const totalSeconds =
        Math.round(seconds);

    const hours =
        Math.floor(
            totalSeconds / 3600
        );

    const minutes =
        Math.floor(
            (totalSeconds % 3600) / 60
        );

    const remainingSeconds =
        totalSeconds % 60;

    if (hours > 0) {

        return `${hours}h ${minutes}m`;
    }

    if (minutes > 0) {

        return `${minutes} min`;
    }

    return `${remainingSeconds} sec`;
}