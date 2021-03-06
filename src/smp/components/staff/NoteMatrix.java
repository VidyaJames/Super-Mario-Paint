package smp.components.staff;

import java.util.ArrayList;

import smp.ImageLoader;
import smp.components.Values;
import smp.components.staff.sequences.StaffAccidental;
import smp.components.staff.sequences.StaffNote;
import smp.components.staff.sequences.StaffNoteLine;
import smp.stateMachine.StateMachine;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * This is the matrix of notes behind the staff. We can change the
 * size of this dynamically, or by setting it manually. The reason we
 * even have this class is to make it really easy to just refer to some
 * note on the staff via a coordinate system.
 * The schema for addressing notes is: <br>
 * <b>First number</b>: The vertical line that we are addressing;
 * 0 is the left-most line. <br>
 * <b>Second number</b>: The note number that we are addressing.
 * 0 is the lowest note. In regular SMP, this is the low A note.
 * Random fun fact: some of this was coded at 33000 feet.
 * @author RehdBlob
 * @since 2013.08.18
 *
 */
public class NoteMatrix {

    /**
     * The list of lists that holds the different <code>StackPane</code>
     * objects.
     */
    private ArrayList<ArrayList<StackPane>> matrix;

    /** This is the matrix of flats / sharps / etc. */
    private ArrayList<ArrayList<StackPane>> accMatrix;

    /** This is the list of volume bars on the staff. */
    private ArrayList<StackPane> volumeBars;

    /** This is the list of volume bar handlers on the staff. */
    private ArrayList<StaffVolumeEventHandler> volumeBarHandlers;

    /** The number of lines in this note matrix. */
    private int numberOfLines;

    /** The number of notes that are in each note line. */
    private int numberOfNotes;

    /** Pointer to the staff object with which this is linked. */
    private transient Staff theStaff;

    /** Pointer to the current StaffInstrumentEventHandler in focus. */
    private transient StaffInstrumentEventHandler focusPane;

    /**
     * @param x The number of note lines on the current staff.
     * @param y The number of addressable notes in a line.
     */
    public NoteMatrix(int x, int y, Staff s) {
        theStaff = s;
        numberOfLines = x;
        numberOfNotes = y;
        matrix = new ArrayList<ArrayList<StackPane>>();
        accMatrix = new ArrayList<ArrayList<StackPane>>();
        volumeBars = new ArrayList<StackPane>();
        volumeBarHandlers = new ArrayList<StaffVolumeEventHandler>();
    }

    /**
     * Gets you an object based on the coordinate that
     * you give this method. This method should help a lot when working
     * on those portions of code that ask the entire staff to update its
     * images. Bypassing the individual StackPane object links should be
     * a lot easier with this here.
     * @param x The note line number.
     * @param y The note number.
     * @return Index 0 is the <code>StackPane</code> of the note that
     * is located at the location. Index 1 is the <code>StackPane</code>
     * of the flat / sharp / etc box that it is associated with.
     */
    public StackPane[] getNote(int x, int y) {
        StackPane note;
        StackPane acc;
        note = matrix.get(x).get(Values.NOTES_IN_A_LINE - y - 1);
        acc = accMatrix.get(x).get(Values.NOTES_IN_A_LINE - y - 1);
        return new StackPane[] {note, acc};
    }

    /**
     * @param x The index from which to retrieve a volume bar from.
     * @return The volume bar located at that index.
     */
    public StackPane getVolumeBar(int x) {
        return volumeBars.get(x);
    }

    /** @return The number of lines in the note matrix. */
    public int numberOfLines() {
        return numberOfLines;
    }

    /** @return The number of notes in a line. */
    public int numberOfNotes() {
        return numberOfNotes;
    }

    /**
     * Adds a new line of notes into the note matrix.
     * @param newLine The line that we want to add into the note matrix.
     */
    public void addLine(ArrayList<StackPane> newLine) {
        matrix.add(newLine);
    }


    /**
     * Adds a new line of accidentals into the note matrix.
     * @param newLine The line that we want to add into the accidental
     * matrix.
     */
    public void addAccLine(ArrayList<StackPane> newLine) {
        accMatrix.add(newLine);
    }

    /**
     * Removes the line of notes at index <code>index</code>.
     * @param index The index of the line of notes that we want to remove.
     */
    public void removeLine(int index) {
        matrix.remove(index);
        accMatrix.remove(index);
    }

    /**
     * @param index The index from which we want to retrieve a line from.
     * @return A list of <code>StackPane</code>s that exists at that line.
     */
    public ArrayList<StackPane> getLine(int index) {
        return matrix.get(index);
    }

    /** Redraws the entire matrix. */
    public void redraw() {
        for(int i = 0; i < Values.NOTELINES_IN_THE_WINDOW; i++)
            redraw(i);
        if (focusPane != null)
            focusPane.redraw();
    }

    /**
     * Re-draws a certain line in this matrix.
     * @param index The index at which we want to redraw.
     */
    private void redraw(int index) {

        StaffVolumeEventHandler sveh = volumeBarHandlers.get(index);
        int currentPosition = StateMachine.getMeasureLineNum();

        StaffNoteLine stl =
                theStaff.getSequence().getLine(
                        currentPosition + index);

        updateVolumeDisplay(sveh, stl);
        clearNoteDisplay(index);
        populateNoteDisplay(stl, index);



    }

    /**
     * Updates the volume display.
     * @param sveh The StaffVolumeEventHandler that we're dealing with.
     * @param stl The StaffNoteLine that we're dealing with.
     */
    private void updateVolumeDisplay(StaffVolumeEventHandler sveh, StaffNoteLine stl) {
        sveh.setStaffNoteLine(stl);
        sveh.updateVolume();
    }

    /**
     * Clears the note display on the staff.
     * @param index The index that we are clearing.
     */
    private void clearNoteDisplay(int index) {
        ArrayList<StackPane> nt = matrix.get(index);
        ArrayList<StackPane> ac = accMatrix.get(index);
        for (int i = 0; i < Values.NOTES_IN_A_LINE; i++) {
            ObservableList<Node> ntList = nt.get(i).getChildren();
            ObservableList<Node> acList = ac.get(i).getChildren();
            ntList.clear();
            acList.clear();
        }
    }

    /**
     * Repopulates the note display on the staff.
     * @param stl The StaffNoteLine that we are interested in.
     * @param index The index to repopulate.
     */
    private void populateNoteDisplay(StaffNoteLine stl, int index) {
        ArrayList<StaffNote> st = stl.getNotes();
        for (StaffNote s : st) {
            StackPane[] noteAndAcc = getNote(index, s.getPosition());
            noteAndAcc[0].getChildren().add(s);
            StaffAccidental accidental = new StaffAccidental(s);
            accidental.setImage(
                    ImageLoader.getSpriteFX(
                            Staff.switchAcc(s.getAccidental())));
            noteAndAcc[1].getChildren().add(accidental);

            if (s.muteNoteVal() == 0) {
                s.setImage(ImageLoader.getSpriteFX(
                        s.getInstrument().imageIndex()));
            } else if (s.muteNoteVal() == 1) {
                s.setImage(ImageLoader.getSpriteFX(
                        s.getInstrument().imageIndex().alt()));
            } else {
                s.setImage(ImageLoader.getSpriteFX(
                        s.getInstrument().imageIndex().silhouette()));
            }

            s.setVisible(true);
        }
    }

    /**
     * This sets up the volume bars on the main window.
     * @param vol This is the <code>ArrayList</code> of <code>StackPane</code>s
     * that should hold volume bars.
     */
    public void setVolumeBars(ArrayList<StackPane> vol) {
        volumeBars = vol;
    }

    /**
     * Adds a StaffVolumeEventHandler to the list of tracked things.
     * @param sveh The StaffVolumeEventHandler that we want to add.
     */
    public void addVolHandler(StaffVolumeEventHandler sveh) {
        volumeBarHandlers.add(sveh);
    }

    /**
     * @param index The index that we want to query.
     * @return Some StaffVolumeEventHandler hopefully.
     */
    public StaffVolumeEventHandler getVolHandler(int index) {
        return volumeBarHandlers.get(index);
    }

    /**
     * Sets the current focused StaffInstrumentEventHandler.
     * @param st The pane that is currently in focus.
     */
    public void setFocusPane(StaffInstrumentEventHandler st) {
        focusPane = st;
    }

    /** @return The staff that this NoteMatrix is linked to. */
    public Staff getStaff() {
        return theStaff;
    }




}
