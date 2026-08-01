# AI Usage Disclosure

## Tool Used

Claude (Anthropic) — AI Assistant

## How AI Was Used

AI was used as a learning and support tool throughout this project. It helped me:

- Understand assignment requirements and software engineering concepts.
- Explain Java, JDBC, Maven, and testing concepts.
- Review and explain code functionality.
- Assist with debugging compiler errors, test failures, and runtime issues.
- Provide guidance on design decisions and documentation.

## Critical Evaluation

I did not accept AI-generated content without review. All code was checked, tested, and modified where necessary. I used AI primarily to improve my understanding of the project and the technologies used. Final implementation decisions, testing, verification, and submission were completed by me.

For example, when a bug was found where uploading a profile picture created a duplicate profile row instead of updating the existing one, I identified the incorrect behaviour through testing in the browser, worked with the AI assistant to diagnose the root cause in `JdbcProfileRepository.save()`, and verified the fix myself by re-running the test suite and manually testing the corrected behaviour before committing the change.

## Learning Outcome

AI supported my learning and development process, but I was responsible for understanding, implementing, testing, and validating the final solution.