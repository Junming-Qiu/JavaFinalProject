# JavaFinalProject
<h1>Epic 'Not a Mario Game' Game</h1>
<h2>Game Rules</h2>
<ol>
    <li>2 Players connect and are placed on opposite corners on a nxn grid</li>
    <li>Players are represented by a random color and can move across the grid 1 tile at a time in the up, down, left, right direction. If player is against a wall, it cannot move any further</li>
    <li>There are coins randomly scattered across the grid that players must collect</li>
    <li>The players must collect all the coins on the grid within a time limit and then meet each other on the same grid to win the game</li>
</ol>
<h2>Server</h2>
<ol>
    <li>When server starts up, it begins looking for 2 connections from clients</li>
    <li>When 2 connections are found, it begins the game by spinning up a timer and receiver thread for each client</li>
    <li>Inits a thread safe array representing a nxn board to store game state</li>
    <li>Receive: receives a direction from each client concurrently and updates its own state according to game rules. If move is illegal, make no change</li>
    <li>Send: First send player color/shape information to clients. For every update to the state, sends updated state to the client. A separate thread will sent each client information about the timer. This timer thread will also send whether or not the clients won or lost based on the server state when it gets to 0. Timer will send increments of 5min, 1min, 30sec...</li>
    <li>Once the game ends, the server will close current connections and look for 2 new connections, repeating from step 1</li>
</ol>

<h2>Client</h2>
<ol>
    <li>When first launched, opens a connect screen displaying a text input to enter server IP. There is also a start button that will make the client attempt to connect to the server</li>
    <li>Once connected, client spins up a receiver thread to update its state based on the server messages. Client should always overlay/replace the server's memory of where client is with client's most recent location before rendering the new grid</li>
    <li>Client will open a game window showing the grid using graphics, and also 4 buttons/keyboard controls to send a message to the server to update its state. Each player has a different color/shape, as determined by the server</li>
    <li>The client is also receiving a timer message from the server on a separate thread, and updating this on the GUI as well</li>
    <li>Once the timer ends, the server will send an end of game message, in which the client will close the game window and show a win/lose screen</li>
</ol>

<h2>Notes</h2>
