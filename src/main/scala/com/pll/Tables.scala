import slick.driver.H2Driver.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}

class Radios(tag : Tag) extends Table[(Int, String, Option[String])](tag, "RADIOS") {
  def rad_id : Rep[Int] = column[Int]("RADIOID", O.PrimaryKey)
  def alias : Rep[String] = column[String]("RADIOALIAS")
  def location : Rep[Option[String]] = column[Option[String]]("RADIOLOCATION")

  def * : ProvenShape[(Int, String, Option[String])] = (rad_id, alias, location)
}

class Locations(tag : Tag) extends Table[(Int, String)](tag, "LOCATIONS") {
  def rad_id : Rep[Int] = column[Int]("RADIOID")
  def location : Rep[String] = column[String]("LOCATION")

  def * : ProvenShape[(Int, String)] = (rad_id, location)
  def pk = primaryKey("PK_LOC", (rad_id, location))
  def radio : ForeignKeyQuery[Radios, (Int, String, Option[String])] =
    foreignKey("ID_FK", rad_id, TableQuery[Radios])(_.rad_id)
}
