package goblinbob.mba.editor;

public interface IEditorAction<T>
{
    void perform(T editor);
    void undo(T editor);
}
