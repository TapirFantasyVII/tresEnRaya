// =======================
// AGENT: IAPlayer
// =======================
// This agent implements a simple tic-tac-toe player using the minimax algorithm.
// It can play as either 'x' or 'o', and will calculate the best move based on the current board state.
// =======================

// =======================
// BELIEFS
// =======================
// Initial beliefs for this agent 
otherSymbol(o) :- mySymbol(x).
otherSymbol(x) :- mySymbol(o).

// Current state of the board
realBoardState(fila(emp,emp,emp), fila(emp,emp,emp), fila(emp,emp,emp)).
 
// =======================
// VALID MOVES
// =======================
validMove(0,0,fila(emp,_,_),_,_).
validMove(0,1,fila(_,emp,_),_,_).
validMove(0,2,fila(_,_,emp),_,_).
validMove(1,0,_,fila(emp,_,_),_).
validMove(1,1,_,fila(_,emp,_),_).
validMove(1,2,_,fila(_,_,emp),_).
validMove(2,0,_,_,fila(emp,_,_)).
validMove(2,1,_,_,fila(_,emp,_)).
validMove(2,2,_,_,fila(_,_,emp)).

// =======================
// UPDATE BOARD
// =======================
updateBoard(0,0,V,fila(emp,B,C),fila(D,E,F),fila(G,H,I),fila(V,B,C),fila(D,E,F),fila(G,H,I)).
updateBoard(0,1,V,fila(A,emp,C),fila(D,E,F),fila(G,H,I),fila(A,V,C),fila(D,E,F),fila(G,H,I)).
updateBoard(0,2,V,fila(A,B,emp),fila(D,E,F),fila(G,H,I),fila(A,B,V),fila(D,E,F),fila(G,H,I)).
updateBoard(1,0,V,fila(A,B,C),fila(emp,E,F),fila(G,H,I),fila(A,B,C),fila(V,E,F),fila(G,H,I)).
updateBoard(1,1,V,fila(A,B,C),fila(D,emp,F),fila(G,H,I),fila(A,B,C),fila(D,V,F),fila(G,H,I)).
updateBoard(1,2,V,fila(A,B,C),fila(D,E,emp),fila(G,H,I),fila(A,B,C),fila(D,E,V),fila(G,H,I)).
updateBoard(2,0,V,fila(A,B,C),fila(D,E,F),fila(emp,H,I),fila(A,B,C),fila(D,E,F),fila(V,H,I)).
updateBoard(2,1,V,fila(A,B,C),fila(D,E,F),fila(G,emp,I),fila(A,B,C),fila(D,E,F),fila(G,V,I)).
updateBoard(2,2,V,fila(A,B,C),fila(D,E,F),fila(G,H,emp),fila(A,B,C),fila(D,E,F),fila(G,H,V)).

// =======================
// GAME END CONDITIONS
// =======================
endGame :- drawState(R1,R2,R3) & realBoardState(R1,R2,R3).
endGame :- winState(R1,R2,R3,P) & realBoardState(R1,R2,R3) & (P = x | P = o).

drawState(fila(A,B,C),fila(D,E,F),fila(G,H,I)) :-
    not A = emp & not B = emp & not C = emp &
    not D = emp & not E = emp & not F = emp &
    not G = emp & not H = emp & not I = emp.

winState(fila(X1,X2,X3),fila(X4,X5,X6),fila(X7,X8,X9),P) :-
    (X1 = X2 & X2 = X3 & X1 = P) |
    (X4 = X5 & X5 = X6 & X4 = P) |
    (X7 = X8 & X8 = X9 & X7 = P) |
    (X1 = X4 & X4 = X7 & X1 = P) |
    (X2 = X5 & X5 = X8 & X2 = P) |
    (X3 = X6 & X6 = X9 & X3 = P) |
    (X1 = X5 & X5 = X9 & X1 = P) |
    (X3 = X5 & X5 = X7 & X3 = P).

// =======================
// START
// =======================
!start.
+!start : meFirst <- 
    .print("I go first"); 
    !calculateMove(X,Y);
    .print("I play: ", X, ",", Y);
    !playMove(X,Y).
+!start <- .print("waiting for opponent").

// =======================
// RECEIVE MOVE
// =======================
+played(Simb, X, Y) : otherSymbol(Simb) & realBoardState(R1,R2,R3) <-
    ?updateBoard(X,Y,Simb,R1,R2,R3,NR1,NR2,NR3);
    !updateRealBoard(NR1,NR2,NR3);
    !calculateMove(X2,Y2);
    !playMove(X2,Y2).

// =======================
// PLAY MOVE
// =======================
+!playMove(X,Y) : mySymbol(Simb) & realBoardState(R1,R2,R3) <-
    ?updateBoard(X,Y,Simb,R1,R2,R3,NR1,NR2,NR3);
    !updateRealBoard(NR1,NR2,NR3);
    .print("Playing move: ", X, ",", Y);
    play(X,Y);
    .wait(1000);
    .broadcast(tell, played(Simb,X,Y)).

// =======================
// UPDATE BOARD STATE
// =======================
+!updateRealBoard(NR1,NR2,NR3) <-
    -realBoardState(_,_,_);
    +realBoardState(NR1,NR2,NR3).

// =======================
// CALCULATE MOVE (MINIMAX)
// =======================
+!calculateMove(BestX,BestY) : mySymbol(Simb) & realBoardState(R1,R2,R3) <-
    .print("Calculating best move...");
    .findall(move(Score,X,Y), 
        (
            validMove(X,Y,R1,R2,R3) & 
            updateBoard(X,Y,Simb,R1,R2,R3,NR1,NR2,NR3) & 
            minimax(NR1,NR2,NR3,false,Score)
        ), Moves);
    .print("Possible moves: ", Moves);
    !selectBest(Moves,BestX,BestY,_).

// =======================
// MINIMAX
// =======================
minimax(R1,R2,R3,_,1) :- mySymbol(Simb) & winState(R1,R2,R3,Simb).
minimax(R1,R2,R3,_,-1) :- otherSymbol(Simb) & winState(R1,R2,R3,Simb).
minimax(R1,R2,R3,_,0) :- drawState(R1,R2,R3).

// MAX
minimax(R1,R2,R3,true,Score) :- mySymbol(Simb) &
    .findall(S, (
        validMove(X,Y,R1,R2,R3) & 
        updateBoard(X,Y,Simb,R1,R2,R3,NR1,NR2,NR3) & 
        minimax(NR1,NR2,NR3,false,S)
    ), Scores) & .max(Scores,Score).

// MIN
minimax(R1,R2,R3,false,Score) :- otherSymbol(Simb) &
    .findall(S, (
        validMove(X,Y,R1,R2,R3) & 
        updateBoard(X,Y,Simb,R1,R2,R3,NR1,NR2,NR3) & 
        minimax(NR1,NR2,NR3,true,S)
    ), Scores) & .min(Scores,Score).

// =======================
// SELECT BEST MOVE
// =======================
+!selectBest(Moves,BestX,BestY,BestScore) : not endGame <-
    .max(Moves,move(BestScore,BestX,BestY)).

