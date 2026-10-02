package inoxcal

import dev.tamboui.style.Style
import dev.tamboui.terminal.AnsiStringBuilder
import dev.tamboui.text.{Line, Span, Text}
import java.util.Arrays

/** Plain and TamboUI-backed renderers for structured calendar data. */
object CalendarTextRenderer:
  private val WeekNumberWidth = 2

  def renderMonth(view: MonthView): String =
    val header = s"    ${view.monthName} ${view.year}"
    val dayHeader = view.weekdays.map(view.l10n.getDayName).mkString(" ")
    val weekHeader = if view.weekNumbers then s"Wk $dayHeader" else dayHeader
    val rows = view.weeks.map { week =>
      val dayCells = week.days.map(_.map(date => f"${date.getDayOfMonth}%2d").getOrElse("  ")).mkString(" ")
      if view.weekNumbers then f"${week.weekNumber.getOrElse(0)}%2d $dayCells" else dayCells
    }
    (Vector(header, weekHeader) ++ rows).mkString("\n")

  def renderMonthColored(view: MonthView): String =
    val lines = Vector.newBuilder[Line]
    lines += Line.from(Span.styled(s"    ${view.monthName} ${view.year}", Style.create().cyan().bold()))
    val dayHeader = view.weekdays.map(view.l10n.getDayName).mkString(" ")
    val weekHeader = if view.weekNumbers then s"Wk $dayHeader" else dayHeader
    lines += Line.from(Span.styled(weekHeader, Style.create().bold()))
    view.weeks.foreach { week =>
      val spans = Vector.newBuilder[Span]
      if view.weekNumbers then
        spans += Span.styled(f"${week.weekNumber.getOrElse(0)}%2d ", Style.create().magenta())
      week.days.foreach {
        case Some(date) =>
          val dayStyle =
            if date.equals(java.time.LocalDate.now()) then Style.create().reversed().bold()
            else if date.getDayOfWeek.getValue % 7 == 0 then Style.create().red()
            else if date.getDayOfWeek.getValue % 7 == 6 then Style.create().yellow()
            else Style.EMPTY
          spans += Span.styled(f"${date.getDayOfMonth}%2d ", dayStyle)
        case None => spans += Span.raw("   ")
      }
      lines += Line.from(Arrays.asList(spans.result()*))
    }
    val text = Text.from(Arrays.asList(lines.result()*))
    text.lines().toArray.map(_.asInstanceOf[Line]).map(renderLine).mkString("\n")

  private def renderLine(line: Line): String =
    line.spans().toArray.map(_.asInstanceOf[Span]).map { span =>
      val ansi = AnsiStringBuilder.styleToAnsi(span.style())
      if ansi.isEmpty then span.content() else s"$ansi${span.content()}${AnsiStringBuilder.RESET}"
    }.mkString.stripSuffix(" ")