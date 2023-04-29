# JavaFinalProject
<h1>Epic 'Not a Mario Game' Game</h1>
<h2>Game Rules</h2>
<ol>
    <li>2 Players connect and are placed randomly on a nxn grid</li>
    <li>Players are represented by a random color and can move across the grid 1 tile at a time in the up, down, left, right direction</li>
    <li>There are coins randomly scattered across the grid that players must collect</li>
    <li>The players must collect all the coins on the grid within a time limit and then meet each other on the same grid to win the game</li>
</ol>
<h2>Server</h2>
<ol>
    <li>When server starts up, it begins looking for 2 connections from clients</li>
    <li>When 2 connections are found, it begins the game by spinning up a timer and receiver thread for each client</li>
    <li>Receive: receives a direction from each client concurrently and updates its own state</li>
    <li>Send: For every update to the state, sends updated state to the client. A separate thread will sent each client information about the timer. This timer thread will also send whether or not the clients won or lost based on the server state when it gets to 0</li>
    <li>Once the game ends, the server</li>

</ol>

<h2>Client</h2>