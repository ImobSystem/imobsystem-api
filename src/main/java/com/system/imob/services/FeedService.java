package com.system.imob.services;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;
import com.system.imob.enums.TipoImovel;
import com.system.imob.models.FotoImovel;
import com.system.imob.models.Imobiliaria;
import com.system.imob.models.Imovel;
import com.system.imob.repositories.ImobiliariaRepository;
import com.system.imob.repositories.ImovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Gera o feed XML no formato ListingDataFeed, consumido pelo ZAP Imóveis,
 * VivaReal e OLX (Grupo ZAP). Os portais leem a URL a cada 12 horas.
 */
@Service
public class FeedService {

    // Os portais esperam ponto decimal. Sem Locale fixo, uma JVM pt-BR
    // escreveria "350000,00" e o preço chegaria errado (ou seria recusado).
    private static final Locale LOCALE_XML = Locale.US;

    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private ImobiliariaRepository imobiliariaRepository;

    public String gerarFeedXml(Long imobiliariaId) {
        Imobiliaria imob = imobiliariaRepository.findById(imobiliariaId)
                .orElseThrow(() -> new RuntimeException("Imobiliária não encontrada"));

        List<Imovel> imoveis = imovelRepository.findByImobiliariaId(imobiliariaId)
                .stream()
                .filter(i -> Boolean.TRUE.equals(i.getPublicarPortais()))
                .filter(i -> i.getStatusImovel() != StatusImovel.FECHADO)
                .toList();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<ListingDataFeed>\n");

        xml.append("  <Header>\n");
        xml.append("    <Provider>Kaza System</Provider>\n");
        xml.append("    <Email>").append(escapeXml(imob.getEmail())).append("</Email>\n");
        xml.append("    <ContactName>").append(escapeXml(imob.getNome())).append("</ContactName>\n");
        xml.append("  </Header>\n");

        xml.append("  <Listings>\n");
        for (Imovel imovel : imoveis) {
            xml.append(gerarListingXml(imovel, imob));
        }
        xml.append("  </Listings>\n");
        xml.append("</ListingDataFeed>");

        return xml.toString();
    }

    private String gerarListingXml(Imovel imovel, Imobiliaria imob) {
        StringBuilder xml = new StringBuilder();
        xml.append("    <Listing>\n");
        xml.append("      <ListingID>").append(imovel.getId()).append("</ListingID>\n");
        xml.append("      <Title>").append(escapeXml(gerarTitulo(imovel))).append("</Title>\n");

        String transactionType = imovel.getFinalidade() == Finalidade.VENDA ? "For Sale" : "For Rent";
        xml.append("      <TransactionType>").append(transactionType).append("</TransactionType>\n");

        if (imovel.getFotos() != null && !imovel.getFotos().isEmpty()) {
            xml.append("      <Media>\n");
            for (FotoImovel foto : imovel.getFotos()) {
                xml.append("        <Item medium=\"image\">").append(escapeXml(foto.getUrl())).append("</Item>\n");
            }
            xml.append("      </Media>\n");
        }

        xml.append("      <Details>\n");
        xml.append("        <PropertyType>").append(mapearTipoImovel(imovel.getTipoImovel())).append("</PropertyType>\n");
        if (imovel.getDescricao() != null) {
            xml.append("        <Description><![CDATA[")
                    .append(escapeCdata(imovel.getDescricao()))
                    .append("]]></Description>\n");
        }
        if (imovel.getValor() != null) {
            if (imovel.getFinalidade() == Finalidade.VENDA) {
                xml.append("        <ListPrice currency=\"BRL\">")
                        .append(decimal(imovel.getValor())).append("</ListPrice>\n");
            } else {
                xml.append("        <RentalPrice currency=\"BRL\" period=\"Monthly\">")
                        .append(decimal(imovel.getValor())).append("</RentalPrice>\n");
            }
        }
        // area_m2 é opcional na entidade; sem a guarda, String.format escreveria "null"
        if (imovel.getArea_m2() != null) {
            xml.append("        <LivingArea unit=\"square metres\">")
                    .append(decimal(imovel.getArea_m2())).append("</LivingArea>\n");
        }
        if (imovel.getQuartos() != null) xml.append("        <Bedrooms>").append(imovel.getQuartos()).append("</Bedrooms>\n");
        if (imovel.getBanheiros() != null) xml.append("        <Bathrooms>").append(imovel.getBanheiros()).append("</Bathrooms>\n");
        if (imovel.getVagasGaragem() != null) xml.append("        <Garage>").append(imovel.getVagasGaragem()).append("</Garage>\n");
        xml.append("      </Details>\n");

        xml.append("      <Location>\n");
        xml.append("        <Address>").append(escapeXml(imovel.getEndereco())).append("</Address>\n");
        if (imovel.getBairro() != null) xml.append("        <Neighborhood>").append(escapeXml(imovel.getBairro())).append("</Neighborhood>\n");
        if (imovel.getCidade() != null) xml.append("        <City>").append(escapeXml(imovel.getCidade())).append("</City>\n");
        if (imovel.getEstado() != null) xml.append("        <State>").append(escapeXml(imovel.getEstado())).append("</State>\n");
        xml.append("        <PostalCode>").append(escapeXml(imovel.getCEP())).append("</PostalCode>\n");
        xml.append("        <Country>BR</Country>\n");
        xml.append("      </Location>\n");

        xml.append("      <ContactInfo>\n");
        xml.append("        <Name>").append(escapeXml(imob.getNome())).append("</Name>\n");
        xml.append("        <Email>").append(escapeXml(imob.getEmail())).append("</Email>\n");
        if (imob.getTelefone() != null) {
            xml.append("        <Telephone>").append(escapeXml(imob.getTelefone())).append("</Telephone>\n");
        }
        xml.append("      </ContactInfo>\n");

        xml.append("    </Listing>\n");
        return xml.toString();
    }

    private String mapearTipoImovel(TipoImovel tipo) {
        if (tipo == null) return "Residential / Home";
        return switch (tipo) {
            case APARTAMENTO -> "Residential / Apartment";
            case CASA -> "Residential / Home";
            case TERRENO -> "Land / Residential";
            case SALA_COMERCIAL -> "Commercial / Office";
            case LOJA -> "Commercial / Retail";
            case GALPAO -> "Commercial / Industrial";
            case COBERTURA -> "Residential / Penthouse";
            case KITNET -> "Residential / Apartment";
            case SITIO -> "Land / Farm";
            case FAZENDA -> "Land / Farm";
        };
    }

    private String gerarTitulo(Imovel imovel) {
        StringBuilder titulo = new StringBuilder();
        if (imovel.getTipoImovel() != null) {
            titulo.append(switch (imovel.getTipoImovel()) {
                case APARTAMENTO -> "Apartamento";
                case CASA -> "Casa";
                case TERRENO -> "Terreno";
                case SALA_COMERCIAL -> "Sala Comercial";
                case LOJA -> "Loja";
                case GALPAO -> "Galpão";
                case COBERTURA -> "Cobertura";
                case KITNET -> "Kitnet";
                case SITIO -> "Sítio";
                case FAZENDA -> "Fazenda";
            });
        } else {
            titulo.append("Imóvel");
        }
        if (imovel.getQuartos() != null && imovel.getQuartos() > 0) {
            titulo.append(" ").append(imovel.getQuartos()).append(" quarto");
            if (imovel.getQuartos() > 1) titulo.append("s");
        }
        if (imovel.getBairro() != null) {
            titulo.append(" no ").append(imovel.getBairro());
        }
        return titulo.toString();
    }

    private String decimal(Double valor) {
        return String.format(LOCALE_XML, "%.2f", valor);
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    // Um "]]>" digitado na descrição fecharia o CDATA no meio e derrubaria o feed
    // inteiro da imobiliária. O truque é fechar e reabrir a seção em volta dele.
    private String escapeCdata(String text) {
        if (text == null) return "";
        return text.replace("]]>", "]]]]><![CDATA[>");
    }
}
