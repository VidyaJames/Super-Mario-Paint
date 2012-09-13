package smp.components.controls;

import smp.stateMachine.State;

public class Controls {

    private PlayButton play;
    private StopButton stop;
    public Controls() {

    }

    public void changeState(State s) {
        switch(s) {
        case EDITING:
            break;
        case SONG_PLAYING:
            break;
        case ARR_PLAYING:
            break;
        case PAUSE:
            break;
        case EDITING_ADV:
            break;
        case SONG_PLAYING_ADV:
            break;
        case ARR_PLAYING_ADV:
            break;
        case PAUSE_ADV:
            break;
        default:
            break;
        }
    }

}
