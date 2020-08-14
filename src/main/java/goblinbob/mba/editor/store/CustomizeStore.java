package goblinbob.mba.editor.store;

import goblinbob.mba.editor.viewport.AlterEntryRig;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.flux.IObserver;
import goblinbob.mobends.core.flux.Subscription;
import goblinbob.mobends.core.store.Store;

public class CustomizeStore extends Store<CustomizeState>
{

    public static CustomizeStore instance = new CustomizeStore();

    private CustomizeStore()
    {
        super(new CustomizeState()
        {
            {
                currentAnimatedEntity.next(null);
                rig.next(null);
                selectedPart.next(null);
            }
        });
    }

    public static Subscription observeAnimatedEntity(IObserver<EntityBender<?>> observer)
    {
        return instance.state.currentAnimatedEntity.subscribe(observer);
    }

    public static Subscription observeAlterEntryRig(IObserver<AlterEntryRig> observer)
    {
        return instance.state.rig.subscribe(observer);
    }

    public static Subscription observeSelectedPart(IObserver<AlterEntryRig.Bone> observer)
    {
        return instance.state.selectedPart.subscribe(observer);
    }

    public static EntityBender<?> getCurrentAnimatedEntity()
    {
        return instance.state.currentAnimatedEntity.getValue();
    }

    public static AlterEntryRig getRig() { return instance.state.rig.getValue(); }

    public static AlterEntryRig.Bone getSelectedPart()
    {
        return instance.state.selectedPart.getValue();
    }

    public static String[] getAlterableParts()
    {
        EntityBender animatedEntity = instance.state.currentAnimatedEntity.getValue();
        if (animatedEntity != null)
            return animatedEntity.getAlterableParts();
        else
            return null;
    }

    public static boolean areChangesUnapplied()
    {
        return instance.state.changesMade;
    }

}
