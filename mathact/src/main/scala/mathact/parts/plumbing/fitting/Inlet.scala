/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2016 CAB      *
 *          # #      # #                                  #  #                     *
 *         #  #    #  #           # #     # #           #     #              # #   *
 *        #   #  #   #             #       #          #        #              #    *
 *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
 *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
 *     #          #   # # # #   #       #      #  #           #  #         #       *
 *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
 *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
 * @                                                                             @ *
\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */

package mathact.parts.plumbing.fitting

import scala.concurrent.Future


/** Contains event handler.
  * Created by CAB on 13.05.2016.
  */









  //!!! Возможно стоит перенести эти методы в Connector (или его скрытое расширение), и использовать как flange.connect(inlet)
  //так же перенести в Connector disconnect
  //Концептуально это будет правельно: Connector это штука которая соединяет/разьединяет Inlet и Outlet

  //В целом стоит сделать MaleFlange к коотрому будет приводится Outlet FemaleFlange к которому будет приводится Inlet,
  //каждяй из интерфейсов будет иметь методы connect и disconnect, которые можно будет вызывать откуда угодно,
  //таким образом можно будет соддавть не только выходы но и вход, кторы можно будет соединять из вне инструментов,
  //что очень полезно при графическом редактирвоаниа.
  //При этом соедиение с помощью DSL остаётся возможным, просто вход будет определеятся внутри субинстуменати
  //и не будет виден с наружы.
  //Входы и выходы создаются подобным способом, хелперами Inlet и Outlet, и захватывают контекст текущего инструмента,
  //гед они определены, в чатности помпу котрая буде процессировать обработку событий


//  //Простой медод без синхронизации (один Connector один Inlet)
//  def apply[Z, A](inlet: Z with Inlet[A], in: ()⇒Plug[A]): Z with Jack[A] = {  //Связание Connector с Inlet
//
//
//
//    in match{
//      case o: Outlet[_] ⇒      //Подключнение (чтобы достать помпу из Outlet)
//      case _ ⇒}   //Ошибка
//
//
//
//    ???
//
//  }



  //Методы ниже принимают несколько Connector и компонуют их в один Inlet,
  //Нужно перерабоать их чтобы они поддержывали следующие виды компоновка:
  // 1) Барьерная - pours вызывается только когда на всех выходах есть значение, после вызова сохранённые значения сбрасывабтся
  // 2) Барьерная с сохраением - pours не вызыватся пока все значения не заполнятся, после заполения вызывается
  //    при при изменеии любого из занчений, вместе с новым передаются сохранённые значения
  // 3) Для каждого - pours каждый раз когда приходи новое значение по одному из входов, это значение не пустое остальные
  //    пустые, значения сбрасываются после вызова.
  // 4) Для каждого для каждого с сохранением - тоже но перд значение созраняются, и будут переданы при следующем вызове

  //!!!Чтобы не иметь кучю методов в одном стоит определеить несколько классов-соеденителей (по аналогии с водопроводныим двойниками,
  // тройтиками т.п.) берущих один или несколько Inlet и вприводящихся к одному Outlet. Выполняющих смешывание и мапинг событий,
  // Т.е. Inlet сам оп семе может соединется  толко с одним Outlet.
  //Код внутри инструмента будет выглядить пример так:
  //  class Line2(name: String) extends Inlet[(Double, String)]{
  //    def pours(v: (Double, String)): Unit = println("Handle: " + v)
  //
  //    def of(in1: ⇒Connector[Double], in2: ⇒Connector[String]): Unit = {
  //
  //      connect(new Mixer(in1,in2))    //Mixer компонует два Connector[Double] в один Connector[Double], который подключается к Inlet
  //
  //    }
  //
  //
  //    val out: MaleFlange = Outlet( new Outlet{ push(...)} )     //Пример определеия выхода
  //
  //    val in: FemaleFlange = Inlet( new Inlet{ def pours(v) = ...} )     //Пример определеия входа
  //
  //    out.connect(in)      //Соедиение выхода и входа
  //
  //    in.connect(out)      //Тоже
  //
  //     val out2: MaleFlange = ...
  //
  //     val in2: FemaleFlange = ...
  //
  //     in.connect(out); in2.connect(out)  //Один выход может быть подключен к двум входам
  //
  //     in.connect(out); in.connect(out2)  //Ошибка. нельзя просто подключать несколько выходов к одному входу
  //
  //     in.connect(Mix(out, out2))         //Нужно использовать класс-компановщик (или определеить свой).
  //
  //  }
  //
  //  Хелперы Inlet и Outlet нужны чтобы захватывать помпу из контекста инатумента,
  //  потому при подключении внутри инструмета (как в методе def of выше) Inlet должен быть другим класом.
  //  не наследующим FemaleFlange (чтобы с его помощью нельзя бвло создвать входы), и меть свой метод
  //  connect|disconnect которые и будут захватывать контекст и подключать вход.
  //  но с другой стороны можно инжектить контекст при создание нового экземпляра:
  //    def line(name:string) = Inlet( new Line2(name) )    //Возвращает Line2 и далее можно вызывать def of,
  //  что в целом эквиваленто внешнему подключению, но вход не торчит в наружу.
  //  Тогда опредляемие внути входы будут создавтся так:
  //    Inlet(new Inlet{ def pours(v) = ...})  //Т.е. возвращамое FemaleFlange заячение не используется, так как
  //    функции def pours доступны все внурености класа
  //
  //

//
//  def apply[Z,A](inlet: Z with Inlet[A], in:(()⇒Plug[A])*): Z = { //Связание нескольких Connector с Inlet
//
//    ???
//
//  }
//
//
//  def apply[Z,A,B](inlet: Z with Inlet[(A,B)], in1: ()⇒Plug[A], in2: ()⇒Plug[B]): Z = { //Связание нескольких Connector разных типов с Inlet
//
//    ???
//
//  }






trait Inlet[T] extends Jack[T] with Pipe[T]{   //Методы обьявдены protected чтобы из не вызывали из вне, но пользователь может реализовть свой методв и оставить его доступным из вне

  private[mathact] def processValue(value: Any): Unit = pours(value.asInstanceOf[T])





//  pours(value)


  protected def pours(value: T): Unit    //Вызыватеся каждый раз при получении нового значения из Connector



//  protected def disconnect(flange: Connector[_]): Boolean = ???    //Отключение указаного Connector, true если было выполенео, false если не найдено
//  protected def disconnectAll: Boolean = ???    //Отключение dct[ Connector, true если было выполенео, false если нет ни одного


  //??? Нужны ли методы ниже.
  protected def lastValue: Option[T] = ???      //Возвращает последнее полученое значение
  protected def nextValue: Future[T] = ???      //Ожыдание следующего значения




//    println(in())









//}
//  def disconnect(in:()⇒Connector[Double]): Unit = {
//
//  ???
//
//}


}