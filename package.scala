package blog

import cats.effect.{Fiber, IO, IOApp, ExitCode}
import scala.concurrent.duration._
object AsynchronousIOs extends IOApp {

    val meaningOfLife: IO[Int] = IO(42)
    val favLang: IO[String] = IO("Scala")

    def createFiber: Fiber[IO, Throwable, String] = ???

    extension [A] (io: IO[A])
        def debug: IO[A] = io.map { value =>
            println(s"[${Thread.currentThread().getName()}] $value")
            value
        
        }
    
    def sameThread() = for {
        _ <- meaningOfLife.debug
        _ <- favLang.debug
    } yield ()

    val aFiber: IO[Fiber[IO, Throwable, Int]] = meaningOfLife.debug.start  // IO[Fiber[IO, Throable, Int]]

    def differentThreads() = for {
        _ <- aFiber
        _ <- favLang.debug
    }

    def runOnAnotherThread[A](io: IO[A]) = for {
        fib <- io.start // fib = Fiber
        result <- fib.join
    } yield result


    def throwOnAnotherThread() = for {
        fib <- IO.raiseError[Int](new RuntimeException("No number for you. ")).start
        result <- fib.join
    } yield result

    def testCancel() = {
        val task = IO("starting").debug *> IO.sleep(1.second) *> IO("done").debug

        for {
            fib <- task.start
            _ <- IO.sleep(500.millis) *> IO("cencelling").debug
            _ <- fib.cancel
            result <- fib.join
        } yield result 
    }


    def run(args: List[String]): IO[ExitCode] = 
        testCancel().as(ExitCode.Success)
    }
    

