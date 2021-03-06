package ru.mtl.VoidVoice.dao;

import net.sf.autodao.AutoDAO;
import net.sf.autodao.Dao;
import net.sf.autodao.Finder;
import ru.mtl.VoidVoice.model.Vector3d;

import java.util.List;

@AutoDAO
public interface Vector3dDao extends Dao<Vector3d, Long> {
    @Finder(query = "from Vector3d")
    List<Vector3d> getAll();

    @Finder(query="select count(*) from Vector3d")
    long count();
}
