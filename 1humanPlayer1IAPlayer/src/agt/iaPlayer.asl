// IAPlayer — Minimax + Alpha-Beta

otherSymbol(o) :- mySymbol(x).
otherSymbol(x) :- mySymbol(o).

realBoardState(board(fila(emp,emp,emp),fila(emp,emp,emp),fila(emp,emp,emp))).

validMove(0,0,board(fila(emp,_,_),_,_)).
validMove(0,1,board(fila(_,emp,_),_,_)).
validMove(0,2,board(fila(_,_,emp),_,_)).
validMove(1,0,board(_,fila(emp,_,_),_)).
validMove(1,1,board(_,fila(_,emp,_),_)).
validMove(1,2,board(_,fila(_,_,emp),_)).
validMove(2,0,board(_,_,fila(emp,_,_))).
validMove(2,1,board(_,_,fila(_,emp,_))).
validMove(2,2,board(_,_,fila(_,_,emp))).

updateBoard(0,0,V,board(fila(emp,B,C),R2,R3),board(fila(V,B,C),R2,R3)).
updateBoard(0,1,V,board(fila(A,emp,C),R2,R3),board(fila(A,V,C),R2,R3)).
updateBoard(0,2,V,board(fila(A,B,emp),R2,R3),board(fila(A,B,V),R2,R3)).
updateBoard(1,0,V,board(R1,fila(emp,B,C),R3),board(R1,fila(V,B,C),R3)).
updateBoard(1,1,V,board(R1,fila(A,emp,C),R3),board(R1,fila(A,V,C),R3)).
updateBoard(1,2,V,board(R1,fila(A,B,emp),R3),board(R1,fila(A,B,V),R3)).
updateBoard(2,0,V,board(R1,R2,fila(emp,B,C)),board(R1,R2,fila(V,B,C))).
updateBoard(2,1,V,board(R1,R2,fila(A,emp,C)),board(R1,R2,fila(A,V,C))).
updateBoard(2,2,V,board(R1,R2,fila(A,B,emp)),board(R1,R2,fila(A,B,V))).

drawState(board(fila(A,B,C),fila(D,E,F),fila(G,H,I))) :-
    not A=emp & not B=emp & not C=emp & not D=emp & not E=emp &
    not F=emp & not G=emp & not H=emp & not I=emp.

winState(board(fila(P,P,P),_,_),P).
winState(board(_,fila(P,P,P),_),P).
winState(board(_,_,fila(P,P,P)),P).
winState(board(fila(P,_,_),fila(P,_,_),fila(P,_,_)),P).
winState(board(fila(_,P,_),fila(_,P,_),fila(_,P,_)),P).
winState(board(fila(_,_,P),fila(_,_,P),fila(_,_,P)),P).
winState(board(fila(P,_,_),fila(_,P,_),fila(_,_,P)),P).
winState(board(fila(_,_,P),fila(_,P,_),fila(P,_,_)),P).

!start.
+!start : meFirst <- !move.
+!start <- .print("Waiting opponent...").

+played(S,X,Y) : otherSymbol(S) & realBoardState(B) & not gameOver <-
    ?updateBoard(X,Y,S,B,NB); -realBoardState(_); +realBoardState(NB);
    if (winState(NB,S)) {
        +gameOver; .my_name(Me); .print("[",Me,"] I lost! ",S," wins.");
        .broadcast(tell, gameResult(S,wins));
    } else { if (drawState(NB)) {
        +gameOver; .my_name(Me); .print("[",Me,"] Draw!");
        .broadcast(tell, gameResult(nobody,draw));
    } else { !move; } }.

+!move : mySymbol(S) & realBoardState(B) <-
    !best(B,S,X,Y); .print("Chosen move: ",X,",",Y);
    ?updateBoard(X,Y,S,B,NB); -realBoardState(_); +realBoardState(NB);
    play(X,Y); .broadcast(tell, played(S,X,Y));
    if (winState(NB,S)) {
        +gameOver; .my_name(Me); .print("[",Me,"] I won! ",S," wins.");
        .broadcast(tell, gameResult(S,wins));
    } else { if (drawState(NB)) {
        +gameOver; .my_name(Me); .print("[",Me,"] Draw!");
        .broadcast(tell, gameResult(nobody,draw));
    } }.

+gameResult(W,wins) : mySymbol(W)    <- +gameOver; .my_name(Me); .print("[",Me,"] Confirmed: I won.").
+gameResult(W,wins) : otherSymbol(W) <- +gameOver; .my_name(Me); .print("[",Me,"] Confirmed: I lost.").
+gameResult(_,draw)                  <- +gameOver; .my_name(Me); .print("[",Me,"] Confirmed: draw.").

+!best(B,S,OX,OY) <-
    .findall(pos(X,Y), validMove(X,Y,B), M); .shuffle(M,SM);
    !root(SM,B,S,-1,1,-2,0,0,OX,OY).

+!root([],_,_,_,_,_,BX,BY,BX,BY).
+!root([pos(X,Y)|R],B,S,A,Be,BS,BX,BY,OX,OY) <-
    ?updateBoard(X,Y,S,B,NB); !minAB(NB,false,A,Be,Sc);
    if (Sc > BS) { NBS = Sc; NBX = X; NBY = Y; } else { NBS = BS; NBX = BX; NBY = BY; }
    if (Sc > A)  { NA  = Sc; } else { NA  = A;  }
    if (NA >= Be) { OX = NBX; OY = NBY; } else { !root(R,B,S,NA,Be,NBS,NBX,NBY,OX,OY); }.

+!minAB(B,_,_,_, 1) : mySymbol(S)    & winState(B,S).
+!minAB(B,_,_,_,-1) : otherSymbol(S) & winState(B,S).
+!minAB(B,_,_,_, 0) : drawState(B).
+!minAB(B,true ,A,Be,O) : mySymbol(S)    <- .findall(pos(X,Y),validMove(X,Y,B),M); !maxL(M,B,S,A,Be,-1,O).
+!minAB(B,false,A,Be,O) : otherSymbol(S) <- .findall(pos(X,Y),validMove(X,Y,B),M); !minL(M,B,S,A,Be, 1,O).

+!maxL([],_,_,_,_,C,O) <- O = C.
+!maxL([pos(X,Y)|R],B,S,A,Be,C,O) <-
    ?updateBoard(X,Y,S,B,NB); !minAB(NB,false,A,Be,Sc);
    if (Sc > C)  { NC = Sc; } else { NC = C;  }
    if (Sc > A)  { NA = Sc; } else { NA = A;  }
    if (NA >= Be) { O = NC; } else { !maxL(R,B,S,NA,Be,NC,O); }.

+!minL([],_,_,_,_,C,O) <- O = C.
+!minL([pos(X,Y)|R],B,S,A,Be,C,O) <-
    ?updateBoard(X,Y,S,B,NB); !minAB(NB,true,A,Be,Sc);
    if (Sc < C)  { NC  = Sc; } else { NC  = C;  }
    if (Sc < Be) { NBe = Sc; } else { NBe = Be; }
    if (NBe <= A) { O = NC; } else { !minL(R,B,S,A,NBe,NC,O); }.
