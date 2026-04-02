// =======================
// AGENT: IAPlayer (Alpha-Beta)
// =======================

otherSymbol(o) :- mySymbol(x).
otherSymbol(x) :- mySymbol(o).

realBoardState(board(fila(emp,emp,emp), fila(emp,emp,emp), fila(emp,emp,emp))).

// =======================
// VALID MOVES
// =======================
validMove(0,0,board(fila(emp,_,_),_,_)).
validMove(0,1,board(fila(_,emp,_),_,_)).
validMove(0,2,board(fila(_,_,emp),_,_)).
validMove(1,0,board(_,fila(emp,_,_),_)).
validMove(1,1,board(_,fila(_,emp,_),_)).
validMove(1,2,board(_,fila(_,_,emp),_)).
validMove(2,0,board(_,_,fila(emp,_,_))).
validMove(2,1,board(_,_,fila(_,emp,_))).
validMove(2,2,board(_,_,fila(_,_,emp))).

// =======================
// UPDATE BOARD
// =======================
updateBoard(0,0,V,board(fila(emp,B,C),fila(D,E,F),fila(G,H,I)),board(fila(V,B,C),fila(D,E,F),fila(G,H,I))).
updateBoard(0,1,V,board(fila(A,emp,C),fila(D,E,F),fila(G,H,I)),board(fila(A,V,C),fila(D,E,F),fila(G,H,I))).
updateBoard(0,2,V,board(fila(A,B,emp),fila(D,E,F),fila(G,H,I)),board(fila(A,B,V),fila(D,E,F),fila(G,H,I))).
updateBoard(1,0,V,board(fila(A,B,C),fila(emp,E,F),fila(G,H,I)),board(fila(A,B,C),fila(V,E,F),fila(G,H,I))).
updateBoard(1,1,V,board(fila(A,B,C),fila(D,emp,F),fila(G,H,I)),board(fila(A,B,C),fila(D,V,F),fila(G,H,I))).
updateBoard(1,2,V,board(fila(A,B,C),fila(D,E,emp),fila(G,H,I)),board(fila(A,B,C),fila(D,E,V),fila(G,H,I))).
updateBoard(2,0,V,board(fila(A,B,C),fila(D,E,F),fila(emp,H,I)),board(fila(A,B,C),fila(D,E,F),fila(V,H,I))).
updateBoard(2,1,V,board(fila(A,B,C),fila(D,E,F),fila(G,emp,I)),board(fila(A,B,C),fila(D,E,F),fila(G,V,I))).
updateBoard(2,2,V,board(fila(A,B,C),fila(D,E,F),fila(G,H,emp)),board(fila(A,B,C),fila(D,E,F),fila(G,H,V))).

// =======================
// GAME END
// =======================
drawState(board(fila(A,B,C),fila(D,E,F),fila(G,H,I))) :-
    not A = emp & not B = emp & not C = emp &
    not D = emp & not E = emp & not F = emp &
    not G = emp & not H = emp & not I = emp.

winState(board(fila(X1,X2,X3),fila(X4,X5,X6),fila(X7,X8,X9)),P) :-
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
    !playMove(X,Y).

+!start <-
    .print("Waiting opponent...").

// =======================
// RECEIVE MOVE 
// =======================
+played(Simb,X,Y) : otherSymbol(Simb) & realBoardState(Board) & not gameOver <-
    ?updateBoard(X,Y,Simb,Board,NewBoard);
    !updateRealBoard(NewBoard);  

    if (winState(NewBoard, Simb)) {
        +gameOver;
        .my_name(Me);
        .print("[", Me, "] I lost! ", Simb, " wins.");
        .broadcast(tell, gameResult(Simb, wins));
    } elif (drawState(NewBoard)) {
         +gameOver;
         .my_name(Me);
         .print("[", Me, "] It's a draw!");
         .broadcast(tell, gameResult(nobody, draw));
        } else {
            !calculateMove(X2,Y2);
            !playMove(X2,Y2);
        }
    .

// =======================
// PLAY MOVE
// =======================
+!playMove(X,Y) : mySymbol(Simb) & realBoardState(Board) <-
    ?updateBoard(X,Y,Simb,Board,NewBoard);
    !updateRealBoard(NewBoard);
    .wait(1000);
    play(X,Y);
    .broadcast(tell, played(Simb,X,Y));

    if (winState(NewBoard, Simb)) {
        +gameOver;
        .my_name(Me);
        .print("[", Me, "] I won! ", Simb, " wins.");
        .broadcast(tell, gameResult(Simb, wins));
    } else {
        if (drawState(NewBoard)) {
            +gameOver;
            .my_name(Me);
            .print("[", Me, "] It's a draw!");
            .broadcast(tell, gameResult(nobody, draw));
        }
    }.

// =======================
// RECEIVE GAME RESULT 
// =======================
+gameResult(Who, wins) : mySymbol(Who) <-
    .my_name(Me);
    .print("[", Me, "] Confirmed: I won!");
    +gameOver.

+gameResult(Who, wins) : not mySymbol(Who) <-
    .my_name(Me);
    .print("[", Me, "] Confirmed: I lost.");
    +gameOver.

+gameResult(nobody, draw) <-
    .my_name(Me);
    .print("[", Me, "] Confirmed: draw.");
    +gameOver.

// =======================
// UPDATE REAL BOARD
// =======================
+!updateRealBoard(NewBoard) <-
    -realBoardState(_);
    +realBoardState(NewBoard).

// =======================
// CALCULATE MOVE
// =======================
+!calculateMove(BestX,BestY) : mySymbol(Simb) & realBoardState(Board) <-
    !bestMove(Board,Simb,-2,2,none,BestX,BestY,_);
    .print("Chosen move: ", BestX, ",", BestY).

// =======================
// BEST MOVE
// =======================
+!bestMove(Board,Simb,Alpha,Beta,CurrentBest,BestX,BestY,BestScore) <-
    .findall(pos(X,Y), validMove(X,Y,Board), Moves);
    .shuffle(Moves, ShuffledMoves);
    !evalMoves(ShuffledMoves,Board,Simb,Alpha,Beta,CurrentBest,BestX,BestY,BestScore).

+!evalMoves([],_,_,_,_,move(S,X,Y),X,Y,S).

+!evalMoves([pos(X,Y)|Rest],Board,Simb,Alpha,Beta,CurrentBest,BestX,BestY,BestScore) <-
    ?updateBoard(X,Y,Simb,Board,NewBoard);
    !minAB(NewBoard,false,Alpha,Beta,Score);

    ?chooseBest(Score,X,Y,CurrentBest,NewBest);

    if (Score > Alpha) {
        NewAlpha = Score;
    } else {
        NewAlpha = Alpha;
    }

    if (NewAlpha >= Beta) {
        ?extract(NewBest,BestX,BestY,BestScore);
    } else {
        !evalMoves(Rest,Board,Simb,NewAlpha,Beta,NewBest,BestX,BestY,BestScore);
    }.

// =======================
// MINIMAX
// =======================
+!minAB(Board,_,_,_,1) : mySymbol(S) & winState(Board,S).
+!minAB(Board,_,_,_,-1) : otherSymbol(S) & winState(Board,S).
+!minAB(Board,_,_,_,0) : drawState(Board).

+!minAB(Board,true,Alpha,Beta,Score) : mySymbol(Simb) <-
    .findall(pos(X,Y), validMove(X,Y,Board), Moves);
    !loopMax(Moves,Board,Simb,Alpha,Beta,-2,Score).

+!minAB(Board,false,Alpha,Beta,Score) : otherSymbol(Simb) <-
    .findall(pos(X,Y), validMove(X,Y,Board), Moves);
    !loopMin(Moves,Board,Simb,Alpha,Beta,2,Score).

// =======================
// MAX LOOP
// =======================
+!loopMax([],_,_,_,_,BestIn,BestOut) <-
    BestOut = BestIn.

+!loopMax([pos(X,Y)|Rest],Board,Simb,Alpha,Beta,Current,BestOut) <-
    ?updateBoard(X,Y,Simb,Board,NewBoard);
    !minAB(NewBoard,false,Alpha,Beta,Score);

    if (Score > Current) {
        NewCurrent = Score;
    } else {
        NewCurrent = Current;
    }

    if (Score > Alpha) {
        NewAlpha = Score;
    } else {
        NewAlpha = Alpha;
    }

    if (NewAlpha >= Beta) {
        BestOut = NewCurrent;
    } else {
        !loopMax(Rest,Board,Simb,NewAlpha,Beta,NewCurrent,BestOut);
    }.

// =======================
// MIN LOOP
// =======================
+!loopMin([],_,_,_,_,BestIn,BestOut) <-
    BestOut = BestIn.

+!loopMin([pos(X,Y)|Rest],Board,Simb,Alpha,Beta,Current,BestOut) <-
    ?updateBoard(X,Y,Simb,Board,NewBoard);
    !minAB(NewBoard,true,Alpha,Beta,Score);

    if (Score < Current) {
        NewCurrent = Score;
    } else {
        NewCurrent = Current;
    }

    if (Score < Beta) {
        NewBeta = Score;
    } else {
        NewBeta = Beta;
    }

    if (NewBeta <= Alpha) {
        BestOut = NewCurrent;
    } else {
        !loopMin(Rest,Board,Simb,Alpha,NewBeta,NewCurrent,BestOut);
    }.

// =======================
// HELPERS
// =======================
chooseBest(S,X,Y,none,move(S,X,Y)).
chooseBest(S,X,Y,move(S2,_,_),move(S,X,Y)) :- S > S2.
chooseBest(S,_,_,move(S2,X2,Y2),move(S2,X2,Y2)) :- S <= S2.

extract(move(S,X,Y),X,Y,S).