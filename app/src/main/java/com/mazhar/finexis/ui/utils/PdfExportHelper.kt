package com.mazhar.finexis.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.mazhar.finexis.model.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportHelper {

    fun exportTransactionsToPdf(
        context: Context,
        expenses: List<Expense>,
        currency: String,
        totalIncome: Double,
        totalExpenses: Double,
        savings: Double
    ) {
        if (expenses.isEmpty()) {
            Toast.makeText(context, "No transactions to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            // 1. Setup Paints
            val primaryPaint = Paint().apply {
                color = Color.rgb(0, 107, 68) // Emerald Green
                style = Paint.Style.FILL
            }
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                color = Color.argb(204, 255, 255, 255)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            val headerPaint = Paint().apply {
                color = Color.rgb(33, 33, 33)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val headerTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = Color.rgb(55, 65, 81) // Charcoal Gray
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            val cardBgPaint = Paint().apply {
                color = Color.rgb(243, 244, 246) // Light Gray
                style = Paint.Style.FILL
            }
            val incomePaint = Paint().apply {
                color = Color.rgb(22, 163, 74) // Green
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val expensePaint = Paint().apply {
                color = Color.rgb(220, 38, 38) // Red
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val savingsPaint = Paint().apply {
                color = Color.rgb(13, 148, 136) // Teal
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val dividerPaint = Paint().apply {
                color = Color.rgb(229, 231, 235) // Border Gray
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }
            val rowBgPaint = Paint().apply {
                color = Color.rgb(249, 250, 251) // Very Light Gray
                style = Paint.Style.FILL
            }

            // Date formatting
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val currentDateStr = timestampFormat.format(Date())

            // Start First Page
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            // Draw Top Banner
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, primaryPaint)
            canvas.drawText("Finexis Transaction Report", 30f, 40f, titlePaint)
            canvas.drawText("Generated on: " + currentDateStr, 30f, 60f, subtitlePaint)
            canvas.drawText("Currency: " + currency, 30f, 75f, subtitlePaint)

            // Draw Financial Summary Cards (Top = 110f, Bottom = 175f)
            val cardY = 110f
            val cardHeight = 60f
            val cardWidth = 160f
            val spacing = 20f

            // Income Card
            val incomeLeft = 30f
            canvas.drawRoundRect(incomeLeft, cardY, incomeLeft + cardWidth, cardY + cardHeight, 10f, 10f, cardBgPaint)
            canvas.drawText("TOTAL INCOME", incomeLeft + 15f, cardY + 25f, bodyPaint)
            canvas.drawText(CurrencyHelper.format(totalIncome, currency, showDecimal = false), incomeLeft + 15f, cardY + 45f, incomePaint)

            // Expenses Card
            val expenseLeft = incomeLeft + cardWidth + spacing
            canvas.drawRoundRect(expenseLeft, cardY, expenseLeft + cardWidth, cardY + cardHeight, 10f, 10f, cardBgPaint)
            canvas.drawText("TOTAL EXPENSES", expenseLeft + 15f, cardY + 25f, bodyPaint)
            canvas.drawText(CurrencyHelper.format(totalExpenses, currency, showDecimal = false), expenseLeft + 15f, cardY + 45f, expensePaint)

            // Savings Card
            val savingsLeft = expenseLeft + cardWidth + spacing
            canvas.drawRoundRect(savingsLeft, cardY, savingsLeft + cardWidth, cardY + cardHeight, 10f, 10f, cardBgPaint)
            canvas.drawText("TOTAL SAVINGS", savingsLeft + 15f, cardY + 25f, bodyPaint)
            canvas.drawText(CurrencyHelper.format(savings, currency, showDecimal = false), savingsLeft + 15f, cardY + 45f, savingsPaint)

            // Table Header setup (Y = 195f)
            var tableHeaderY = 195f
            val tableRowHeight = 25f

            fun drawTableHeader(c: Canvas, y: Float) {
                // Table header background
                c.drawRect(30f, y, (pageWidth - 30).toFloat(), y + tableRowHeight, primaryPaint)
                val textY = y + 16f
                c.drawText("Date", 45f, textY, headerTextPaint)
                c.drawText("Category", 130f, textY, headerTextPaint)
                c.drawText("Description", 230f, textY, headerTextPaint)
                c.drawText("Method", 390f, textY, headerTextPaint)
                headerTextPaint.textAlign = Paint.Align.RIGHT
                c.drawText("Amount", (pageWidth - 45).toFloat(), textY, headerTextPaint)
                headerTextPaint.textAlign = Paint.Align.LEFT
            }

            drawTableHeader(canvas, tableHeaderY)

            // Draw Transactions List
            var currentY = tableHeaderY + tableRowHeight
            val itemsPerPageOnFirstPage = 22 // 195f to 800f height can accommodate approx 22 rows
            val itemsPerPageOnLaterPages = 26 // No summary cards, table starts at 100f

            expenses.forEachIndexed { index, expense ->
                // Check pagination bounds
                val bottomLimit = pageHeight - 50f
                if (currentY + tableRowHeight > bottomLimit) {
                    // Draw Footer on current page
                    val footerText = "Page " + pageNumber
                    canvas.drawText(footerText, (pageWidth / 2 - 15).toFloat(), (pageHeight - 25).toFloat(), bodyPaint)
                    canvas.drawText("Finexis App", 30f, (pageHeight - 25).toFloat(), bodyPaint)

                    pdfDocument.finishPage(page)

                    // Start New Page
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    // New Page Header Banner (Compact)
                    canvas.drawRect(0f, 0f, pageWidth.toFloat(), 50f, primaryPaint)
                    canvas.drawText("Finexis Transaction Report", 30f, 30f, titlePaint)

                    tableHeaderY = 70f
                    drawTableHeader(canvas, tableHeaderY)
                    currentY = tableHeaderY + tableRowHeight
                }

                // Row Background (zebra striping)
                if (index % 2 == 0) {
                    canvas.drawRect(30f, currentY, (pageWidth - 30).toFloat(), currentY + tableRowHeight, rowBgPaint)
                }

                // Draw Row Border bottom
                canvas.drawLine(30f, currentY + tableRowHeight, (pageWidth - 30).toFloat(), currentY + tableRowHeight, dividerPaint)

                // Date
                val dateStr = dateFormat.format(Date(expense.date))
                canvas.drawText(dateStr, 45f, currentY + 16f, bodyPaint)

                // Category
                canvas.drawText(expense.category, 130f, currentY + 16f, bodyPaint)

                // Description (truncated if too long to prevent overflow)
                val rawDescription = if (expense.description.isNotEmpty()) expense.description else expense.category
                val descWidth = 150f
                val description = truncateText(rawDescription, bodyPaint, descWidth)
                canvas.drawText(description, 230f, currentY + 16f, bodyPaint)

                // Payment Method
                canvas.drawText(expense.paymentMethod, 390f, currentY + 16f, bodyPaint)

                // Amount
                val amountStr = (if (expense.isIncome) "+" else "-") + CurrencyHelper.format(expense.amount, currency, showDecimal = false)
                val amtPaint = if (expense.isIncome) incomePaint else expensePaint
                amtPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(amountStr, (pageWidth - 45).toFloat(), currentY + 16f, amtPaint)
                amtPaint.textAlign = Paint.Align.LEFT

                currentY += tableRowHeight
            }

            // Draw Footer on last page
            val finalFooterText = "Page " + pageNumber
            canvas.drawText(finalFooterText, (pageWidth / 2 - 15).toFloat(), (pageHeight - 25).toFloat(), bodyPaint)
            canvas.drawText("Finexis App", 30f, (pageHeight - 25).toFloat(), bodyPaint)

            pdfDocument.finishPage(page)

            // Save PDF to cache dir
            val file = File(context.cacheDir, "Transaction_Report.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()

            // Open Share Intent
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "com.mazhar.finexis.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Transaction Report"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting PDF: " + e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText(truncated + "...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return if (truncated.isNotEmpty()) truncated + "..." else "..."
    }
}
