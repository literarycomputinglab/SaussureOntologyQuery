package it.cnr.ilc.saussure;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class PDFCreator {

	private Document document;
	private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
			Font.BOLD);
	private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
			Font.NORMAL, BaseColor.RED);
	private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
			Font.BOLD);
	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 10,
			Font.BOLD);
	private static Font normalBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
			Font.BOLD);
	private static Font HeaderFont = new Font(Font.FontFamily.TIMES_ROMAN, 20,
			Font.NORMAL, BaseColor.WHITE);
	private static Font HeaerTableFont = new Font(Font.FontFamily.TIMES_ROMAN,
			14, Font.NORMAL, BaseColor.WHITE);
	private static Font smallText = new Font(Font.FontFamily.TIMES_ROMAN, 11);

	public PDFCreator(int orientation) {
		if (orientation == 1)
			document = new Document(PageSize.A4.rotate());
		else
			document = new Document(PageSize.A4);
	}

	public Document getDocument() {
		return document;
	}

	public void openDocument() {
		document.open();
	}

	public void closeDocument() {
		document.close();
	}

	public void addMetaData() {
		document.addTitle("Lexique de la Terminologie Saussurienne");
		document.addSubject("Lexique de la Terminologie Saussurienne: Interface d'Interrogation");
		document.addKeywords("Saussure, Lexique, Interface d'Interrogation");
		document.addAuthor("Institute for Computational Linguistics");
		document.addCreator("Institute for Computational Linguistics");
	}

	public void addFooterPage() throws DocumentException {
		Paragraph preface = new Paragraph();
		addEmptyLine(preface, 1);
		preface.add(new Paragraph("Rapport généré par "
				+ System.getProperty("user.name") + ", " + new Date(),
				smallBold));
		document.add(preface);
		document.newPage();
	}

	public void addQuestion(String query) throws DocumentException {
		Paragraph preface = new Paragraph();
		addEmptyLine(preface, 1);
		preface.add(new Paragraph(query, normalBold));
		addEmptyLine(preface, 1);
		document.add(preface);
	}

	public void addContent() throws DocumentException {
		Chunk chunk = new Chunk("Lexique de la Terminologie Saussurienne",
				HeaderFont);
		chunk.setBackground(new BaseColor(0, 0, 128));
		document.add(chunk);

	}

	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	public void ontoResultTable(java.util.List<OntoResult> res, int question)
			throws DocumentException, MalformedURLException, IOException {
		//System.out.println("ENTRATO CON VALORE : " + question);
		if ((question == 2) || (question == 4)) {
			PdfPTable table = new PdfPTable(3);
			PdfPCell cell = new PdfPCell(new Paragraph(
					"Résultats de la requête", HeaerTableFont));
			cell.setColspan(3);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBackgroundColor(new BaseColor(0, 0, 128));
			cell.setPadding(10.0f);
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph("Terme Source", HeaerTableFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBackgroundColor(new BaseColor(102, 102, 254));
			cell.setPadding(10.0f);
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph("Type Ontologique",
					HeaerTableFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBackgroundColor(new BaseColor(102, 102, 254));
			cell.setPadding(10.0f);
			table.addCell(cell);
			// colonna inferenza
			cell = new PdfPCell(new Paragraph("Inference",
					HeaerTableFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBackgroundColor(new BaseColor(102, 102, 254));
			cell.setPadding(10.0f);
			//------
			table.addCell(cell);

			for (int i = 0; i < res.size(); i++) {
				table.addCell(res.get(i).getTermine());
				table.addCell(res.get(i).getClasse());
				table.addCell(res.get(i).getInferita());
			}

			document.add(table);

		} else {
			if (question == 5) {
				PdfPTable table = new PdfPTable(3);
				PdfPCell cell = new PdfPCell(new Paragraph(
						"Résultats de la requête", HeaerTableFont));
				cell.setColspan(3);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(0, 0, 128));
				cell.setPadding(10.0f);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Trait", HeaerTableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(102, 102, 254));
				cell.setPadding(10.0f);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Valeur",
						HeaerTableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(102, 102, 254));
				cell.setPadding(10.0f);
				table.addCell(cell);
				// colonna inferenza
				cell = new PdfPCell(new Paragraph("Inference",
						HeaerTableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(102, 102, 254));
				cell.setPadding(10.0f);
				//------
				table.addCell(cell);

				for (int i = 0; i < res.size(); i++) {
					table.addCell(res.get(i).getTratto());
					table.addCell(res.get(i).getValore());
					table.addCell(res.get(i).getInferita());
				}

				document.add(table);
			} else {
				PdfPTable table = new PdfPTable(6);
				PdfPCell cell = new PdfPCell(new Paragraph(
						"Résultats de la requête", HeaerTableFont));
				cell.setColspan(6);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(0, 0, 128));
				cell.setPadding(15.0f);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Terme Source",
						HeaerTableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(102, 102, 254));
				cell.setPadding(15.0f);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Type Ontologique",
						HeaerTableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(102, 102, 254));
				cell.setPadding(15.0f);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Relation", HeaerTableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(102, 102, 254));
				cell.setPadding(15.0f);
				table.addCell(cell);
				cell = new PdfPCell(
						new Paragraph("Terme Cible", HeaerTableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(102, 102, 254));
				cell.setPadding(15.0f);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Type Ontologique",
						HeaerTableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(102, 102, 254));
				cell.setPadding(15.0f);
				table.addCell(cell);
				// colonna inferenza
				cell = new PdfPCell(new Paragraph("Inference",
						HeaerTableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(new BaseColor(102, 102, 254));
				cell.setPadding(15.0f);
				//------
				table.addCell(cell);

				for (int i = 0; i < res.size(); i++) {
					PdfPCell c = new PdfPCell(new Paragraph(res.get(i)
							.getTermine().toString(), smallText));
					PdfPCell c2 = new PdfPCell(new Paragraph(res.get(i)
							.getClasse().toString(), smallText));
					PdfPCell c3 = new PdfPCell(new Paragraph(res.get(i)
							.getRelazione().toString(), smallText));
					PdfPCell c4 = new PdfPCell(new Paragraph(res.get(i)
							.getTermine_target().toString(), smallText));
					PdfPCell c5 = new PdfPCell(new Paragraph(res.get(i)
							.getClasse_target().toString(), smallText));
					PdfPCell c6 = new PdfPCell(new Paragraph(res.get(i)
							.getInferita().toString(), smallText));
					table.addCell(c);
					table.addCell(c2);
					table.addCell(c3);
					table.addCell(c4);
					table.addCell(c5);
					table.addCell(c6);
				}

				document.add(table);
			}
		}

		// URL url = getClass().getResource("PDFHeader.png");
		// Image image = Image.getInstance(url);
	}

}
