package goblinbob.mba.editor.store;

import goblinbob.mba.editor.IEditorAction;
import goblinbob.mba.editor.viewport.AlterEntryRig;
import goblinbob.mobends.core.flux.Observable;
import goblinbob.mobends.core.bender.EntityBender;

import java.util.ArrayList;
import java.util.List;

public class CustomizeState
{

    public final Observable<EntityBender<?>> currentAnimatedEntity = new Observable<>();
    public final Observable<AlterEntryRig> rig = new Observable<>();
    public final Observable<AlterEntryRig.Bone> selectedPart = new Observable<>();

    public final List<IEditorAction> actionHistory = new ArrayList<>();
    public int actionHistoryPointer = 0;
    public boolean changesMade = false;

}
