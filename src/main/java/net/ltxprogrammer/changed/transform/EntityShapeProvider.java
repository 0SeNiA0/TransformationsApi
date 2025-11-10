package net.ltxprogrammer.changed.transform;

public interface EntityShapeProvider {
    ClothingShape.Head getHeadShape();
    ClothingShape.Torso getTorsoShape();
    ClothingShape.Legs getLegsShape();
    ClothingShape.Feet getFeetShape();
}
