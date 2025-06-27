package com.syrmec.mailrelaycleaner.controller;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.syrmec.mailrelaycleaner.model.Contactos;
import com.syrmec.mailrelaycleaner.model.EstadoContacto;
import com.syrmec.mailrelaycleaner.repository.ContactosRepository;
import com.syrmec.mailrelaycleaner.service.MailrelayService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class ContactosController {

    @Autowired
    private MailrelayService mailrelayService;

    @Autowired
    private ContactosRepository contactosRepository;

    @GetMapping("/contactos")
    public String mostrarContactos(Model model) {
        List<Contactos> lista = contactosRepository.findAll();

        for (Contactos c : lista) {
            String nuevoEstado = mailrelayService.obtenerEstadoContacto(c.getEmail());
            try {
                EstadoContacto estado = EstadoContacto.valueOf(nuevoEstado);
                c.setEstado(estado);
                contactosRepository.save(c);
            } catch (IllegalArgumentException e) {
                System.out.println("Estado inválido para " + c.getEmail() + ": " + nuevoEstado);
            }
        }

        model.addAttribute("contactos", lista);
        return "contactos";
    }

    @GetMapping("/contactos/nuevo")
    public String nuevoContactoForm(Model model) {
        model.addAttribute("contacto", new Contactos());
        model.addAttribute("estados", EstadoContacto.values());
        return "nuevo_contacto";
    }

    @PostMapping("/contactos")
    public String guardarContacto(@ModelAttribute Contactos contacto) {
        contacto.setEstado(EstadoContacto.ACTIVO);
        contacto.setUltimaApertura(LocalDateTime.now());
        contactosRepository.save(contacto);
        return "redirect:/contactos";
    }

    @GetMapping("/contactos/editar/{id}")
    public String editarContacto(@PathVariable Long id, Model model) {
        Optional<Contactos> contacto = contactosRepository.findById(id);
        if (contacto.isPresent()) {
            model.addAttribute("contacto", contacto.get());
            model.addAttribute("estados", EstadoContacto.values());
            return "editar_contacto";
        } else {
            return "redirect:/contactos";
        }
    }

    @PostMapping("/contactos/actualizar")
    public String actualizarContacto(@ModelAttribute Contactos contacto) {
        contacto.setUltimaApertura(LocalDateTime.now());
        contactosRepository.save(contacto);
        return "redirect:/contactos";
    }

    @GetMapping("/contactos/eliminar/{id}")
    public String eliminarContacto(@PathVariable Long id) {
        contactosRepository.deleteById(id);
        return "redirect:/contactos";
    }

    @GetMapping("/contactos/exportar")
    public void exportarExcel(HttpServletResponse response) throws Exception {
        List<Contactos> lista = contactosRepository.findAll();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=contactos.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Contactos");

        // Encabezado con estilo
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0x85, (byte) 0xBB, (byte) 0x3A}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setThinBorders(headerStyle);

        XSSFCellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setWrapText(true);
        normalStyle.setVerticalAlignment(VerticalAlignment.TOP);
        setThinBorders(normalStyle);

        XSSFCellStyle eliminadoStyle = workbook.createCellStyle();
        eliminadoStyle.cloneStyleFrom(normalStyle);
        eliminadoStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        eliminadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] titulos = {"ID", "Email", "Estado", "Última Apertura", "Eliminado"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < titulos.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(titulos[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Contactos c : lista) {
            Row row = sheet.createRow(rowNum++);
            boolean eliminado = c.isEliminado();
            CellStyle rowStyle = eliminado ? eliminadoStyle : normalStyle;

            row.createCell(0).setCellValue(c.getId());
            row.createCell(1).setCellValue(c.getEmail());
            row.createCell(2).setCellValue(c.getEstado() != null ? c.getEstado().toString() : "");
            row.createCell(3).setCellValue(c.getUltimaApertura() != null ? c.getUltimaApertura().toString() : "");
            row.createCell(4).setCellValue(eliminado ? "Sí" : "No");

            for (int i = 0; i < 5; i++) {
                row.getCell(i).setCellStyle(rowStyle);
            }
        }

        for (int i = 0; i < titulos.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void setThinBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    @PostMapping("/contactos/importar")
    public String importarCSV(@RequestParam("archivo") MultipartFile archivo, Model model) {
        if (archivo.isEmpty()) {
            model.addAttribute("mensaje", "Por favor selecciona un archivo CSV válido.");
            return "redirect:/contactos";
        }

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {

            CsvToBean<Contactos> csvToBean = new CsvToBeanBuilder<Contactos>(lector)
                    .withType(Contactos.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();               //  ←   sin .withSkipLines(1)

            List<Contactos> contactos = csvToBean.parse();
            contactosRepository.saveAll(contactos);

            model.addAttribute("mensaje",
                    "✅ Importación completada. (" + contactos.size() + " registros)");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("mensaje", "❌ Error al procesar el archivo: " + e.getMessage());
        }

        return "redirect:/contactos";
    }


    // ─────────────────────────────────────────────────────────────────────────────
//  Eliminar varios contactos (POST desde la tabla)
// ─────────────────────────────────────────────────────────────────────────────
    @PostMapping("/contactos/eliminar-multiple")
    public String eliminarMultiple(@RequestParam("ids") List<Long> ids,
                                   Model model) {

        if (ids != null && !ids.isEmpty()) {
            contactosRepository.deleteAllById(ids);
            model.addAttribute("mensaje",
                    "✅ Contactos eliminados (" + ids.size() + ")");
        } else {
            model.addAttribute("mensaje",
                    "⚠️ No se seleccionó ningún contacto.");
        }

        model.addAttribute("contactos", contactosRepository.findAll());
        return "contactos";
    }




}
