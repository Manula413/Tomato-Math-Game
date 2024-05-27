# Tomato Math Game

## Overview

Tomato Math Game is a desktop application designed to make learning math fun and engaging. Developed using Java Swing for the frontend and NetBeans as the IDE, the game retrieves equations in image form from the Tomato API. Players solve these equations by determining the value of tomatoes in the images and inputting their answers using the provided buttons. The game features a user profile system to track scores, a leaderboard to display top players, and two game modes: Easy and Campaign.

## Features

- **Equation Solving:** Players solve equations presented as images with unknown values represented by tomatoes.
- **User Profile:** Track individual player scores.
- **Leaderboard:** View scores of all players and compete for the top spot.
- **Two Game Modes:**
  - **Easy Mode:** No restrictions, players solve equations at their own pace.
  - **Campaign Mode:** Includes time constraints, limited wrong answers per level, and a level-up system.

## Technology Stack

- **Language:** Java
- **Frontend:** Java Swing
- **IDE:** NetBeans
- **Database:** MySQL
- **API:** Tomato API for loading images of equations

## Getting Started

### Prerequisites

- Java Development Kit (JDK)
- NetBeans IDE
- MySQL Server
- Tomato API access

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/TomatoMathGame.git
   cd TomatoMathGame

2.**Set up MySQL Database:**

- Install MySQL Server if not already installed.
- Create a new database named tomato_math_game.
- Run the SQL script provided in the database folder to create necessary tables.

3.**Configure the application:**

- Open the project in NetBeans.
- Configure the database connection in the application properties.

4.**Run the application:**

- Build and run the project from NetBeans.

### Usage
Upon launching the application, players can create or log into their profiles. In Easy mode, they solve equations without any restrictions. In Campaign mode, they face time limits and limited wrong answers, progressing through levels as they correctly solve equations. Players can view their scores and compare them on the leaderboard.
